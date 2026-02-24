package com.flick.business.service;

import com.flick.business.api.dto.request.commercial.SaleRequest;
import com.flick.business.api.dto.response.commercial.SaleResponse;
import com.flick.business.api.dto.response.common.PageResponse;
import com.flick.business.api.dto.response.common.GroupSummary;
import com.flick.business.api.dto.response.common.TotalByPaymentMethod;
import com.flick.business.core.entity.Customer;
import com.flick.business.core.entity.GeneralSettings;
import com.flick.business.core.entity.Product;
import com.flick.business.core.entity.Sale;
import com.flick.business.core.entity.SaleItem;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.core.enums.PaymentStatus;
import com.flick.business.core.enums.settings.StockControlType;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.CustomerRepository;
import com.flick.business.repository.ProductRepository;
import com.flick.business.repository.SaleRepository;
import com.flick.business.repository.spec.SaleSpecification;
import com.flick.business.service.security.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductService productService;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final GeneralSettingsService settingsService;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public SaleResponse registerSale(SaleRequest request) {
        GeneralSettings settings = settingsService.findEntity();
        StockControlType stockControl = settings.getStockControlType();

        Sale newSale = new Sale();
        newSale.setUser(authenticatedUserService.getAuthenticatedUser());
        newSale.setPaymentMethod(request.paymentMethod());
        newSale.setDescription(request.description());

        Customer customer = validateAndGetCustomer(request);
        newSale.setCustomer(customer);

        if (request.paymentMethod() == PaymentMethod.ON_CREDIT) {
            newSale.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            newSale.setPaymentStatus(PaymentStatus.NOT_APPLICABLE);
        }

        BigDecimal totalValue = BigDecimal.ZERO;

        for (var itemRequest : request.items()) {
            Product product = productService.findEntityById(itemRequest.productId());

            boolean isStockManagedForItem = isStockManaged(product, stockControl);

            if (isStockManagedForItem) {
                validateAndDecrementStock(product, itemRequest.quantity());
            }

            SaleItem saleItem = SaleItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getSalePrice())
                    .build();

            newSale.addItem(saleItem);
            totalValue = totalValue.add(saleItem.getTotalValue());
        }

        newSale.setTotalValue(totalValue);
        updateCustomerDebt(customer, newSale);

        Sale savedSale = saleRepository.save(newSale);

        return SaleResponse.fromEntity(savedSale);
    }

    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> listAll(ZonedDateTime startDate, ZonedDateTime endDate, Long customerId,
            String paymentMethodStr,
            String paymentStatusStr,
            Long productId, String orderBy, int page, int size) {

        PaymentMethod paymentMethod = parsePaymentMethod(paymentMethodStr);
        PaymentStatus paymentStatus = parsePaymentStatus(paymentStatusStr);

        Sort sort = createSort(orderBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Sale> spec = SaleSpecification.withFilters(startDate, endDate, customerId, paymentMethod,
                paymentStatus, productId, authenticatedUserService.getAuthenticatedUserId());

        Page<Sale> salePage = saleRepository.findAll(spec, pageable);
        return new PageResponse<>(salePage.map(SaleResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public BigDecimal getGrossTotal(ZonedDateTime startDate, ZonedDateTime endDate, Long customerId,
            String paymentMethodStr, String paymentStatusStr, Long productId) {
        PaymentMethod paymentMethod = parsePaymentMethod(paymentMethodStr);
        PaymentStatus paymentStatus = parsePaymentStatus(paymentStatusStr);

        ZonedDateTime effectiveStartDate = (startDate != null)
                ? startDate
                : ZonedDateTime.parse("1900-01-01T00:00:00Z");

        ZonedDateTime effectiveEndDate = (endDate != null)
                ? endDate
                : ZonedDateTime.parse("9999-12-31T23:59:59Z");

        return saleRepository.getGrossTotalWithFilters(
                effectiveStartDate,
                effectiveEndDate,
                authenticatedUserService.getAuthenticatedUserId(),
                customerId,
                paymentMethod,
                paymentStatus,
                productId);
    }

    @Transactional(readOnly = true)
    public List<TotalByPaymentMethod> getTotalByPaymentMethods(ZonedDateTime startDate, ZonedDateTime endDate) {
        List<Object[]> results = saleRepository.sumTotalGroupByPaymentMethodBetween(
                startDate,
                endDate,
                authenticatedUserService.getAuthenticatedUserId());

        return results.stream()
                .map(res -> new TotalByPaymentMethod((PaymentMethod) res[0], (BigDecimal) res[1]))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupSummary> getSummaryByGroup(ZonedDateTime startDate, ZonedDateTime endDate, Long customerId,
            String paymentMethodStr, Long productId, String groupBy) {
        PaymentMethod paymentMethod = parsePaymentMethod(paymentMethodStr);
        Long userId = authenticatedUserService.getAuthenticatedUserId();

        Specification<Sale> spec = SaleSpecification.withFilters(
                startDate, endDate, customerId, paymentMethod, null, productId, userId);
        List<Sale> sales = saleRepository.findAll(spec);

        String normalizedGroupBy = groupBy == null ? "" : groupBy.trim().toLowerCase(Locale.ROOT);
        Map<String, GroupSummary> groupedSummaries = new LinkedHashMap<>();

        for (Sale sale : sales) {
            String groupKey;
            String groupTitle;

            switch (normalizedGroupBy) {
                case "day", "date" -> {
                    groupKey = sale.getSaleDate().toLocalDate().toString();
                    groupTitle = groupKey;
                }
                case "customer" -> {
                    if (sale.getCustomer() == null) {
                        groupKey = "NO_CUSTOMER";
                        groupTitle = "No customer";
                    } else {
                        groupKey = String.valueOf(sale.getCustomer().getId());
                        groupTitle = sale.getCustomer().getName();
                    }
                }
                case "paymentmethod", "payment_method", "payment" -> {
                    groupKey = sale.getPaymentMethod().name();
                    groupTitle = groupKey;
                }
                default -> throw new BusinessException(
                        "Invalid groupBy parameter. Supported values: day, customer, paymentMethod");
            }

            GroupSummary existing = groupedSummaries.get(groupKey);
            if (existing == null) {
                groupedSummaries.put(groupKey, new GroupSummary(groupKey, groupTitle, sale.getTotalValue()));
            } else {
                groupedSummaries.put(
                        groupKey,
                        new GroupSummary(
                                existing.getGroupKey(),
                                existing.getGroupTitle(),
                                existing.getTotalValue().add(sale.getTotalValue())));
            }
        }

        return groupedSummaries.values().stream()
                .sorted(Comparator.comparing(GroupSummary::getGroupKey))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePermanently(Long saleId) {
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        Sale saleToDelete = saleRepository.findByIdAndUserId(saleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + saleId));

        // restock
        if (saleToDelete.getItems() != null) {
            List<Product> productsToUpdate = new ArrayList<>();
            for (SaleItem item : saleToDelete.getItems()) {
                Product product = item.getProduct();
                // product have stock management = 1 ?
                if (product.isManageStock()) {
                    product.setStockQuantity(product.getStockQuantity().add(item.getQuantity()));
                    productsToUpdate.add(product);
                }
            }
            if (!productsToUpdate.isEmpty()) {
                productRepository.saveAll(productsToUpdate);
            }
        }

        // return credit to customer if aplicable
        if (saleToDelete.getPaymentMethod() == PaymentMethod.ON_CREDIT && saleToDelete.getCustomer() != null) {
            Customer customer = saleToDelete.getCustomer();
            customer.setDebtBalance(customer.getDebtBalance().subtract(saleToDelete.getTotalValue()));
            customerRepository.save(customer);
        }

        // delete sale
        saleRepository.delete(saleToDelete);
    }

    // -- utils --
    private boolean isStockManaged(Product product, StockControlType stockControl) {
        return switch (stockControl) {
            case GLOBAL -> true;
            case PER_ITEM -> product.isManageStock();
            case NONE -> false;
        };
    }

    private void validateAndDecrementStock(Product product, BigDecimal requestedQuantity) {
        if (product.getStockQuantity().compareTo(requestedQuantity) < 0) {
            throw new BusinessException("Insufficient stock for product: " + product.getName() + ". Available: "
                    + product.getStockQuantity() + ", Requested: " + requestedQuantity);
        }

        product.setStockQuantity(product.getStockQuantity().subtract(requestedQuantity));
    }

    private Customer validateAndGetCustomer(SaleRequest request) {
        if (request.paymentMethod() == PaymentMethod.ON_CREDIT && request.customerId() == null) {
            throw new BusinessException("Customer is required for ON_CREDIT sales.");
        }
        return (request.customerId() != null) ? customerService.findEntityById(request.customerId()) : null;
    }

    private void validateStock(Product product, BigDecimal requestedQuantity) {
        if (product.getStockQuantity().compareTo(requestedQuantity) < 0) {
            throw new BusinessException("Insufficient stock for product: " + product.getName() +
                    ". Available: " + product.getStockQuantity() + ", Requested: " + requestedQuantity);
        }
    }

    private void updateCustomerDebt(Customer customer, Sale sale) {
        if (customer != null && sale.getPaymentMethod() == PaymentMethod.ON_CREDIT) {
            if (!customer.getCreditEnabled()) {
                throw new BusinessException("This customer is not enabled for credit purchases.");
            }
            BigDecimal newDebt = customer.getDebtBalance().add(sale.getTotalValue());
            if (customer.getCreditLimit() != null && newDebt.compareTo(customer.getCreditLimit()) > 0) {
                throw new BusinessException("Credit limit exceeded for customer: " + customer.getName());
            }
            customer.setDebtBalance(newDebt);
            customer.setLastCreditPurchaseAt(sale.getSaleDate());
        }
    }

    private Sort createSort(String orderBy) {
        if (orderBy == null || orderBy.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "saleDate");
        }
        String normalized = orderBy.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "dateasc", "date_asc" -> Sort.by(Sort.Direction.ASC, "saleDate");
            case "datedesc", "date_desc" -> Sort.by(Sort.Direction.DESC, "saleDate");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "totalValue");
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "totalValue");
            case "customer_asc" -> Sort.by(Sort.Direction.ASC, "customer.name");
            case "customer_desc" -> Sort.by(Sort.Direction.DESC, "customer.name");
            default -> Sort.by(Sort.Direction.DESC, "saleDate");
        };
    }

    private PaymentMethod parsePaymentMethod(String paymentMethodStr) {
        if (paymentMethodStr == null || paymentMethodStr.isBlank()) {
            return null;
        }
        try {
            return PaymentMethod.valueOf(paymentMethodStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid payment method: " + paymentMethodStr);
        }
    }

    private PaymentStatus parsePaymentStatus(String paymentStatusStr) {
        if (paymentStatusStr == null || paymentStatusStr.isBlank()) {
            return null;
        }
        try {
            return PaymentStatus.valueOf(paymentStatusStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid payment status: " + paymentStatusStr);
        }
    }
}

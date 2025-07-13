package com.flick.business.service;

import com.flick.business.api.dto.request.SaleRequest;
import com.flick.business.api.dto.response.SaleResponse;
import com.flick.business.core.entity.Customer;
import com.flick.business.core.entity.Product;
import com.flick.business.core.entity.Sale;
import com.flick.business.core.entity.SaleItem;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.exception.BusinessException;
import com.flick.business.repository.CustomerRepository;
import com.flick.business.repository.ProductRepository;
import com.flick.business.repository.SaleRepository;
import com.flick.business.repository.spec.SaleSpecification;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductService productService;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public SaleResponse registerSale(SaleRequest request) {
        Sale newSale = new Sale();
        newSale.setPaymentMethod(request.paymentMethod());
        newSale.setDescription(request.description());

        Customer customer = validateAndGetCustomer(request);
        newSale.setCustomer(customer);

        BigDecimal totalValue = BigDecimal.ZERO;
        List<Product> productsToUpdate = new ArrayList<>();

        for (var itemRequest : request.items()) {
            Product product = productService.findEntityById(itemRequest.productId());
            validateStock(product, itemRequest.quantity());

            product.setStockQuantity(product.getStockQuantity().subtract(itemRequest.quantity()));
            productsToUpdate.add(product);

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

        productRepository.saveAll(productsToUpdate);
        Sale savedSale = saleRepository.save(newSale);

        return SaleResponse.fromEntity(savedSale);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponse> listAll(ZonedDateTime startDate, ZonedDateTime endDate, Long customerId,
            String paymentMethodStr, Long productId, String orderBy, int page, int size) {
        PaymentMethod paymentMethod = (paymentMethodStr != null) ? PaymentMethod.valueOf(paymentMethodStr.toUpperCase())
                : null;
        Sort sort = createSort(orderBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Sale> spec = SaleSpecification.withFilters(startDate, endDate, customerId, paymentMethod,
                productId);

        Page<Sale> salePage = saleRepository.findAll(spec, pageable);
        return salePage.map(SaleResponse::fromEntity);
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
            customerRepository.save(customer);
        }
    }

    private Sort createSort(String orderBy) {
        if (orderBy == null || orderBy.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "saleDate");
        }
        return switch (orderBy) {
            case "dateAsc" -> Sort.by(Sort.Direction.ASC, "saleDate");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "totalValue");
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "totalValue");
            case "customer_asc" -> Sort.by(Sort.Direction.ASC, "customer.name");
            case "customer_desc" -> Sort.by(Sort.Direction.DESC, "customer.name");
            default -> Sort.by(Sort.Direction.DESC, "saleDate");
        };
    }
}
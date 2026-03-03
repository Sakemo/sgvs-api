package com.flick.business.service;

import com.flick.business.api.dto.request.commercial.ExpenseRequest;
import com.flick.business.api.dto.response.commercial.ExpenseResponse;
import com.flick.business.api.dto.response.common.PageResponse;
import com.flick.business.api.dto.response.production.RestockItemRequest;
import com.flick.business.api.mapper.ExpenseMapper;
import com.flick.business.core.entity.Expense;
import com.flick.business.core.entity.Product;
import com.flick.business.core.entity.RestockItem;
import com.flick.business.core.entity.security.User;
import com.flick.business.core.enums.ExpenseType;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.ExpenseRepository;
import com.flick.business.repository.ProductRepository;
import com.flick.business.repository.spec.ExpenseSpecification;
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
import java.util.List;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        User currentUser = authenticatedUserService.getAuthenticatedUser();
        Expense expense = new Expense();
        expense.setUser(currentUser);
        expense.setName(request.name());
        expense.setExpenseDate(request.expenseDate());
        expense.setExpenseType(request.expenseType());
        expense.setPaymentMethod(request.paymentMethod());
        expense.setDescription(request.description());

        if (request.expenseType() == ExpenseType.RESTOCKING) {
            processRestockingExpense(expense, request);
        } else {
            processSimpleExpense(expense, request);
        }

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(savedExpense);
    }

    private void processRestockingExpense(Expense expense, ExpenseRequest request) {
        if (request.restockItems() == null || request.restockItems().isEmpty()) {
            throw new BusinessException("Restocking expenses must contain at least one item.");
        }

        List<Product> productsToUpdate = new ArrayList<>();

        for (RestockItemRequest itemRequest : request.restockItems()) {
            Product product = productService.findEntityById(itemRequest.productId());
            BigDecimal quantity = itemRequest.quantity();
            BigDecimal unitCostPrice = itemRequest.unitCostPrice();

            // update stock
            product.setStockQuantity(product.getStockQuantity().add(quantity));
            productsToUpdate.add(product);

            // create and associates RestockItem
            RestockItem restockItem = new RestockItem();
            restockItem.setProduct(product);
            restockItem.setQuantity(quantity);
            restockItem.setUnitCostPrice(unitCostPrice);
            expense.AddRestockItem(restockItem);
        }

        productRepository.saveAll(productsToUpdate);
        applyAutomaticRestockingMetadata(expense);
    }

    @Transactional
    public Product restockProduct(Long productId, BigDecimal quantity, BigDecimal newCostPrice) {
        Product product = productService.findEntityById(productId);

        product.setStockQuantity(product.getStockQuantity().add(quantity));
        product.setCostPrice(newCostPrice);
        return productRepository.save(product);
    }

    private void processSimpleExpense(Expense expense, ExpenseRequest request) {
        if (request.value() == null || request.value().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("A value greater than zero is required for this type of expense.");
        }
        if (request.restockItems() != null && !request.restockItems().isEmpty()) {
            throw new BusinessException("Restock items should not be provided for non-restocking expenses.");
        }
        expense.setValue(request.value());
    }

    @Transactional
    public ExpenseResponse save(ExpenseRequest request) {
        User currentUser = authenticatedUserService.getAuthenticatedUser();
        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(currentUser);
        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(savedExpense);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense existingExpense = findEntityById(id);
        expenseMapper.updateEntityFromRequest(request, existingExpense);
        if (existingExpense.getExpenseType() == ExpenseType.RESTOCKING) {
            applyAutomaticRestockingMetadata(existingExpense);
        }
        Expense updatedExpense = expenseRepository.save(existingExpense);
        return ExpenseResponse.fromEntity(updatedExpense);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> listAll(String name, String expenseTypeStr, ZonedDateTime startDate,
            ZonedDateTime endDate, int page, int size) {
        ExpenseType expenseType = parseExpenseType(expenseTypeStr);
        Specification<Expense> spec = ExpenseSpecification.withFilters(name, startDate, endDate, expenseType, authenticatedUserService.getAuthenticatedUserId());
        Sort sort = Sort.by(Sort.Direction.DESC, "expenseDate");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Expense> expensePage = expenseRepository.findAll(spec, pageable);

        Page<ExpenseResponse> dtoPage = expensePage.map(ExpenseResponse::fromEntity);

        return new PageResponse<>(dtoPage);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotal(ZonedDateTime startDate, ZonedDateTime endDate) {
        ZonedDateTime effectiveStartDate = (startDate != null)
                ? startDate
                : ZonedDateTime.parse("1900-01-01T00:00:00Z");

        ZonedDateTime effectiveEndDate = (endDate != null)
                ? endDate
                : ZonedDateTime.parse("9999-12-31T23:59:59Z");

        return expenseRepository.sumTotalValueBetweenDates(
                effectiveStartDate,
                effectiveEndDate,
                authenticatedUserService.getAuthenticatedUserId());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        return ExpenseResponse.fromEntity(findEntityById(id));
    }

    @Transactional
    public void deleteById(Long id) {
        Expense expense = findEntityById(id);
        expenseRepository.delete(expense);
    }

    private Expense findEntityById(Long id) {
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        return expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
    }

    private ExpenseType parseExpenseType(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) {
            return null;
        }
        try {
            return ExpenseType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid expense type: " + typeStr);
        }
    }

    private void applyAutomaticRestockingMetadata(Expense expense) {
        if (expense.getRestockItems() == null || expense.getRestockItems().isEmpty()) {
            throw new BusinessException("Unable to generate automatic data for restocking expense.");
        }
        RestockItem firstItem = expense.getRestockItems().get(0);
        String firstProductName = firstItem.getProduct() != null ? firstItem.getProduct().getName() : null;
        BigDecimal firstQuantity = firstItem.getQuantity();

        List<String> itemDescriptions = expense.getRestockItems().stream()
                .map(item -> {
                    String productName = item.getProduct() != null ? item.getProduct().getName() : "";
                    return formatQuantity(item.getQuantity()) + "x " + productName.trim();
                })
                .toList();

        BigDecimal totalValue = expense.getRestockItems().stream()
                .map(item -> item.getQuantity().multiply(item.getUnitCostPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        expense.setName(buildAutomaticRestockingName(firstQuantity, firstProductName, expense.getRestockItems().size()));
        expense.setDescription(buildAutomaticRestockingDescription(itemDescriptions));
        expense.setValue(totalValue);
    }

    private String buildAutomaticRestockingName(BigDecimal quantity, String productName, int totalItems) {
        if (quantity == null || productName == null || productName.isBlank()) {
            throw new BusinessException("Unable to generate automatic name for restocking expense.");
        }
        String baseName = formatQuantity(quantity) + "x " + productName.trim();
        if (totalItems <= 1) {
            return baseName;
        }
        return baseName + " +" + (totalItems - 1);
    }

    private String buildAutomaticRestockingDescription(List<String> itemDescriptions) {
        if (itemDescriptions == null || itemDescriptions.isEmpty()) {
            throw new BusinessException("Unable to generate automatic description for restocking expense.");
        }
        StringJoiner joiner = new StringJoiner("\n", "Itens comprados:\n", "");
        itemDescriptions.forEach(item -> joiner.add("- " + item));
        String description = joiner.toString();
        if (description.length() <= 500) {
            return description;
        }
        return description.substring(0, 497) + "...";
    }

    private String formatQuantity(BigDecimal quantity) {
        return quantity.stripTrailingZeros().toPlainString();
    }
}

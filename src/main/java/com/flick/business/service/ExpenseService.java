package com.flick.business.service;

import com.flick.business.api.dto.request.commercial.ExpenseRequest;
import com.flick.business.api.dto.response.commercial.ExpenseResponse;
import com.flick.business.api.dto.response.common.PageResponse;
import com.flick.business.api.dto.response.production.RestockItemRequest;
import com.flick.business.api.mapper.ExpenseMapper;
import com.flick.business.core.entity.Expense;
import com.flick.business.core.entity.Product;
import com.flick.business.core.entity.RestockItem;
import com.flick.business.core.enums.ExpenseType;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.ExpenseRepository;
import com.flick.business.repository.ProductRepository;
import com.flick.business.repository.spec.ExpenseSpecification;
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
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setName(request.name());
        expense.setExpenseDate(request.expenseDate());
        expense.setExpenseType(request.expenseType());
        expense.setPaymentMethod(request.paymentMethod());
        expense.setDescription(request.description());

        if (request.expenseType() == ExpenseType.RESTOCKING) {
            processRestockingExpense(expense, request);
            System.out.println("recognizade restocking: " + expense);
        } else {
            processSimpleExpense(expense, request);
            System.out.println("recognizade not-restocking: " + expense);
        }
        System.out.println("value: " + expense.getValue());
        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(savedExpense);
    }

    /*
     * [DESUSED SINCE hotfix/create-expense | MOD:
     * 18c72a728b6d0b7d85c37e1dbd301f0372a41ffe | COMMITED:
     * a9f056735e0628f8e2e05af6b69a68e88f46bad5]
     * 
     * @Transactional
     * public ExpenseResponse save(ExpenseRequest request) {
     * Expense expense = expenseMapper.toEntity(request);
     * Expense savedExpense = expenseRepository.save(expense);
     * return ExpenseResponse.fromEntity(savedExpense);
     * }
     */

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense existingExpense = findEntityById(id);

        if (existingExpense.getExpenseType() == ExpenseType.RESTOCKING
                && request.expenseType() != ExpenseType.RESTOCKING) {
            reverseStockUpdate(existingExpense);
            existingExpense.getRestockItems().clear();
        }

        mapCommonFields(existingExpense, request);

        if (request.expenseType() == ExpenseType.RESTOCKING) {
            processRestockingExpense(existingExpense, request);
        } else {
            processSimpleExpense(existingExpense, request);
        }
        Expense updatedExpense = expenseRepository.save(existingExpense);
        return ExpenseResponse.fromEntity(updatedExpense);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> listAll(String name, String expenseTypeStr, ZonedDateTime startDate,
            ZonedDateTime endDate, int page, int size) {
        ExpenseType expenseType = parseExpenseType(expenseTypeStr);
        Specification<Expense> spec = ExpenseSpecification.withFilters(name, startDate, endDate, expenseType);
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

        return expenseRepository.sumTotalValueBetweenDates(effectiveStartDate, effectiveEndDate);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        return ExpenseResponse.fromEntity(findEntityById(id));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with ID: " + id);
        }
        expenseRepository.deleteById(id);
    }

    //
    private void mapCommonFields(Expense expense, ExpenseRequest request) {
        expense.setName(request.name());
        expense.setExpenseDate(request.expenseDate());
        expense.setExpenseType(request.expenseType());
        expense.setPaymentMethod(request.paymentMethod());
        expense.setDescription(request.description());
    }

    private void processRestockingExpense(Expense expense, ExpenseRequest request) {
        if (request.restockItems() == null || request.restockItems().isEmpty()) {
            throw new BusinessException("Restocking expenses must contain at least one item.");
        }

        BigDecimal totalValue = BigDecimal.ZERO;
        List<Product> productsToUpdate = new ArrayList<>();

        for (RestockItemRequest itemRequest : request.restockItems()) {
            Product product = productService.findEntityById(itemRequest.productId());
            BigDecimal quantity = itemRequest.quantity();
            BigDecimal unitCostPrice = itemRequest.unitCostPrice();

            // update stock
            product.setStockQuantity(product.getStockQuantity().add(quantity));
            product.setCostPrice(unitCostPrice);
            productsToUpdate.add(product);

            // calculates expense value
            BigDecimal itemTotal = quantity.multiply(unitCostPrice);
            totalValue = totalValue.add(itemTotal);
            System.out.println("LOG LOG LOG LOG        :        " + totalValue);

            // create and associates RestockItem
            RestockItem restockItem = new RestockItem();
            restockItem.setProduct(product);
            restockItem.setQuantity(quantity);
            restockItem.setUnitCostPrice(unitCostPrice);
            expense.AddRestockItem(restockItem);
        }
        expense.setValue(totalValue);
        productRepository.saveAll(productsToUpdate);
    }

    private void reverseStockUpdate(Expense expense) {
        List<Product> productsToRevert = new ArrayList<>();
        for (RestockItem item : expense.getRestockItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity().subtract(item.getQuantity()));
                productsToRevert.add(product);
            }
        }
        if (!productsToRevert.isEmpty()) {
            productRepository.saveAll(productsToRevert);
        }
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

    private Expense findEntityById(Long id) {
        return expenseRepository.findById(id)
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
}
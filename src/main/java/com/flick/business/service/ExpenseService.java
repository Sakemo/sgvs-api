package com.flick.business.service;

import com.flick.business.api.dto.request.ExpenseRequest;
import com.flick.business.api.dto.response.ExpenseResponse;
import com.flick.business.api.mapper.ExpenseMapper;
import com.flick.business.core.entity.Expense;
import com.flick.business.core.enums.ExpenseType;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.ExpenseRepository;
import com.flick.business.repository.spec.ExpenseSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    @Transactional
    public ExpenseResponse save(ExpenseRequest request) {
        Expense expense = expenseMapper.toEntity(request);
        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(savedExpense);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense existingExpense = findEntityById(id);
        expenseMapper.updateEntityFromRequest(request, existingExpense);
        Expense updatedExpense = expenseRepository.save(existingExpense);
        return ExpenseResponse.fromEntity(updatedExpense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listAll(String name, String expenseTypeStr, ZonedDateTime startDate,
            ZonedDateTime endDate) {
        ExpenseType expenseType = parseExpenseType(expenseTypeStr);
        Specification<Expense> spec = ExpenseSpecification.withFilters(name, startDate, endDate, expenseType);
        Sort sort = Sort.by(Sort.Direction.DESC, "expenseDate");
        List<Expense> expenses = expenseRepository.findAll(spec, sort);
        return expenses.stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
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
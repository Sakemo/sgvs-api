package com.flick.business.api.mapper;

import com.flick.business.api.dto.request.ExpenseRequest;
import com.flick.business.core.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {
    public Expense toEntity(ExpenseRequest request) {
        return Expense.builder()
                .name(request.name())
                .value(request.value())
                .expenseDate(request.expenseDate())
                .expenseType(request.expenseType())
                .description(request.description())
                .build();
    }

    public void updateEntityFromRequest(ExpenseRequest request, Expense expense) {
        expense.setName(request.name());
        expense.setValue(request.value());
        expense.setExpenseDate(request.expenseDate());
        expense.setExpenseType(request.expenseType());
        expense.setDescription(request.description());
    }
}
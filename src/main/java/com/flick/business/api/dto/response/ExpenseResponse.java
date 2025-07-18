package com.flick.business.api.dto.response;

import com.flick.business.core.entity.Expense;
import com.flick.business.core.enums.ExpenseType;
import com.flick.business.core.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ExpenseResponse(
        Long id,
        String name,
        BigDecimal value,
        ZonedDateTime expenseDate,
        ExpenseType expenseType,
        PaymentMethod paymentMethod,
        String observation,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt) {
    public static ExpenseResponse fromEntity(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getName(),
                expense.getValue(),
                expense.getExpenseDate(),
                expense.getExpenseType(),
                expense.getPaymentMethod(),
                expense.getDescription(),
                expense.getCreatedAt(),
                expense.getUpdatedAt());
    }
}
package com.flick.business.api.dto.request;

import com.flick.business.core.enums.ExpenseType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ExpenseRequest(
        @NotBlank @Size(min = 3, max = 100) String name,
        @NotNull @DecimalMin("0.01") BigDecimal value,
        @NotNull @PastOrPresent ZonedDateTime expenseDate,
        @NotNull ExpenseType expenseType,
        String description) {
}
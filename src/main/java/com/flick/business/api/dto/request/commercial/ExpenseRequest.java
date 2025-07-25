package com.flick.business.api.dto.request.commercial;

import com.flick.business.api.dto.response.production.RestockItemRequest;
import com.flick.business.core.enums.ExpenseType;
import com.flick.business.core.enums.PaymentMethod;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record ExpenseRequest(
                @NotBlank @Size(min = 3, max = 100) String name,
                BigDecimal value,
                @NotNull @PastOrPresent ZonedDateTime expenseDate,
                @NotNull ExpenseType expenseType,
                @NotNull PaymentMethod paymentMethod,
                String description,
                @Valid List<RestockItemRequest> restockItems) {
}
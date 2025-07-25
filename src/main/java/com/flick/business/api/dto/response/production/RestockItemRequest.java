package com.flick.business.api.dto.response.production;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RestockItemRequest(
                @NotNull Long productId,
                @NotNull @DecimalMin("0.001") BigDecimal quantity,
                @NotNull @DecimalMin("0.01") BigDecimal unitCostPrice) {
}
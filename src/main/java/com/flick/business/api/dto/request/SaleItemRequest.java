package com.flick.business.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SaleItemRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin("0.001") BigDecimal quantity) {
}
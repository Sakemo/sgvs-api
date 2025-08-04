package com.flick.business.api.dto.request.commercial;

import java.math.BigDecimal;
import java.util.List;

import com.flick.business.core.enums.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Long customerId,
        @NotEmpty List<Long> saleIds,
        @NotNull PaymentMethod paymentMethod,
        @NotNull @DecimalMin("0.01") BigDecimal amountPaid) {
}

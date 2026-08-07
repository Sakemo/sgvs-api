package com.flick.business.api.dto.request.commercial;

import com.flick.business.core.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpensePaymentRequest(
        @NotNull PaymentMethod paymentMethod,
        @Size(max = 100) String cashReferenceNumber) {
}

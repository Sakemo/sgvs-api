package com.flick.business.api.dto.request;

import com.flick.business.core.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SaleRequest(
                Long customerId,
                @NotNull PaymentMethod paymentMethod,
                String description,
                @NotEmpty @Valid List<SaleItemRequest> items // Agora importa SaleItemRequest
) {
}
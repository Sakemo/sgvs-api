package com.flick.business.api.dto.request.production;

import jakarta.validation.constraints.NotBlank;

public record ProviderRequest(
    @NotBlank String name,
    String cnpj,
    String notes,
    String phone,
    String email,
    String address
) {
}

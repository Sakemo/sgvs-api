package com.flick.business.api.dto.response.production;

import com.flick.business.core.entity.Provider;

public record ProviderResponse(
    Long id,
    String name,
    String cnpj,
    String notes,
    String phone,
    String email,
    String address
) {
    public static ProviderResponse fromEntity(Provider provider) {
        return new ProviderResponse(
            provider.getId(),
            provider.getName(),
            provider.getCnpj(),
            provider.getNotes(),
            provider.getPhone(),
            provider.getEmail(),
            provider.getAddress()
        );
    }
}

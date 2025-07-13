package com.flick.business.api.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
                @NotBlank @Size(min = 2, max = 100) String name,
                @Size(max = 11) String taxId,
                @Size(max = 11) String phone,
                @Size(max = 150) String address,
                @NotNull Boolean creditEnabled,
                @DecimalMin("0.00") BigDecimal creditLimit,
                @NotNull Boolean active) {
}
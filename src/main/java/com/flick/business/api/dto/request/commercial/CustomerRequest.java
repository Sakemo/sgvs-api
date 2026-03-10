package com.flick.business.api.dto.request.commercial;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Size(max = 18) @Pattern(regexp = "^[0-9.\\-/\\s]*$", message = "Tax ID must contain only digits and punctuation.") String taxId,
        @Size(max = 20) @Pattern(regexp = "^[0-9()+\\-\\s]*$", message = "Phone must contain only digits and punctuation.") String phone,
        @Size(max = 150) String address,
        @NotNull Boolean creditEnabled,
        @DecimalMin("0.00") BigDecimal creditLimit,
        @NotNull Boolean active) {
}

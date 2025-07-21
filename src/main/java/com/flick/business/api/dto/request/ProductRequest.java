package com.flick.business.api.dto.request;

import java.math.BigDecimal;

import com.flick.business.core.enums.UnitOfSale;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
                @NotBlank String name,
                String description,
                String barcode,
                @NotNull @DecimalMin("0.0") BigDecimal stockQuantity,
                @NotNull @DecimalMin("0.01") BigDecimal salePrice,
                @DecimalMin("0.0") BigDecimal costPrice,
                @NotNull UnitOfSale unitOfSale,
                @NotNull Boolean active,
                @NotNull Boolean managesStock,
                @NotNull Long categoryId,
                Long providerId) {
}
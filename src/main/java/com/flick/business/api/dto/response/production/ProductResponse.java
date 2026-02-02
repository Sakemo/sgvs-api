package com.flick.business.api.dto.response.production;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.flick.business.api.dto.response.summary.CategorySummary;
import com.flick.business.api.dto.response.summary.ProviderSummary;
import com.flick.business.core.entity.Product;
import com.flick.business.core.enums.UnitOfSale;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String barcode,
        BigDecimal stockQuantity,
        BigDecimal salePrice,
        BigDecimal costPrice,
        BigDecimal desiredProfitMargin,
        Integer minimumStock,
        UnitOfSale unitOfSale,
        boolean active,
        boolean managesStock,
        CategorySummary category,
        ProviderSummary provider,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt) {
    
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBarcode(),
                product.getStockQuantity(),
                product.getSalePrice(),
                product.getCostPrice(),
                product.getDesiredProfitMargin(),
                product.getMinimumStock(),  
                product.getUnitOfSale(),
                product.isActive(),
                product.isManageStock(),
                product.getCategory() != null
                        ? new CategorySummary(product.getCategory().getId(),
                                product.getCategory().getName())
                        : null,
                product.getProvider() != null
                        ? new ProviderSummary(product.getProvider().getId(),
                                product.getProvider().getName())
                        : null,
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}

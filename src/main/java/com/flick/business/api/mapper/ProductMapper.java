package com.flick.business.api.mapper;

import org.springframework.stereotype.Component;

import com.flick.business.api.dto.request.production.ProductRequest;
import com.flick.business.core.entity.Category;
import com.flick.business.core.entity.Product;
import com.flick.business.core.entity.Provider;

@Component
public class ProductMapper {
    public Product toEntity(ProductRequest request, Category category, Provider provider) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .barcode(request.barcode())
                .active(request.active())
                .manageStock(request.managesStock())
                .salePrice(request.salePrice())
                .costPrice(request.costPrice())
                .desiredProfitMargin(request.desiredProfitMargin())
                .stockQuantity(request.stockQuantity())
                .minimumStock(request.minimumStock())
                .unitOfSale(request.unitOfSale())
                .category(category)
                .provider(provider)
                .build();
    }

    public void updateEntityFromRequest(ProductRequest request, Product product, Category category, Provider provider) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setBarcode(request.barcode());
        product.setActive(request.active());
        product.setManageStock(request.managesStock());
        product.setSalePrice(request.salePrice());
        product.setCostPrice(request.costPrice());
        product.setDesiredProfitMargin(request.desiredProfitMargin());
        product.setStockQuantity(request.stockQuantity());
        product.setMinimumStock(request.minimumStock());
        product.setUnitOfSale(request.unitOfSale());
        product.setCategory(category);
        product.setProvider(provider);
    }
}

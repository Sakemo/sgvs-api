package com.flick.business.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.request.ProductRequest;
import com.flick.business.api.dto.response.ProductResponse;
import com.flick.business.api.mapper.ProductMapper;
import com.flick.business.core.entity.Category;
import com.flick.business.core.entity.Product;
import com.flick.business.core.entity.Provider;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.ProductRepository;
import com.flick.business.repository.spec.ProductSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ProviderService providerService;

    @Transactional
    public ProductResponse save(ProductRequest request) {
        Category category = categoryService.findById(request.categoryId());
        Provider provider = (request.providerId() != null) ? providerService.findById(request.providerId()) : null;

        Product product = productMapper.toEntity(request, category, provider);
        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        Category category = categoryService.findById(request.categoryId());
        Provider provider = (request.providerId() != null) ? providerService.findById(request.providerId()) : null;

        productMapper.updateEntityFromRequest(request, existingProduct, category, provider);
        Product updateProduct = productRepository.save(existingProduct);
        return ProductResponse.fromEntity(updateProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(String name, Long categoryId, String orderBy) {

        if ("mostSold".equalsIgnoreCase(orderBy) || "leastSold".equalsIgnoreCase(orderBy)) {

        }

        Sort sort = createSort(orderBy);
        Specification<Product> spec = ProductSpecification.withFilters(name, categoryId);

        List<Product> products = productRepository.findAll(spec, sort);

        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse copyProduct(Long id) {
        Product original = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product to copy not found with ID: " + id));

        String baseName = original.getName().replaceAll("- Copy \\(\\d+\\)$", "");
        int nextCopyNumber = 1;
        String newName = String.format("%s - Copy (%d)", baseName, nextCopyNumber);

        Product copy = Product.builder()
                .name(newName)
                .description(original.getDescription())
                .salePrice(original.getSalePrice())
                .unitOfSale(original.getUnitOfSale())
                .category(original.getCategory())
                .provider(original.getProvider())
                .active(false)
                .stockQuantity(BigDecimal.ZERO)
                .barcode(null)
                .build();

        Product savedCopy = productRepository.save(copy);
        return ProductResponse.fromEntity(savedCopy);
    }

    @Transactional
    public void toggleActiveStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    @Transactional
    public void deletePermanently(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    private Sort createSort(String orderBy) {
        if (orderBy == null || orderBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }

        return switch (orderBy) {
            case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name");
            case "cheaper" -> Sort.by(Sort.Direction.ASC, "salePrice");
            case "expensiver" -> Sort.by(Sort.Direction.DESC, "salePrice");
            case "older" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "newer" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.ASC, "name");
        };
    }

}

package com.flick.business.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.request.production.ProductRequest;
import com.flick.business.api.dto.response.common.PageResponse;
import com.flick.business.api.dto.response.production.ProductResponse;
import com.flick.business.api.mapper.ProductMapper;
import com.flick.business.core.entity.Category;
import com.flick.business.core.entity.Product;
import com.flick.business.core.entity.Provider;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.ProductRepository;
import com.flick.business.repository.SaleItemRepository;
import com.flick.business.repository.spec.ProductSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ProviderService providerService;
    private final SaleItemRepository saleItemRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> getSuggestions() {
        List<Long> topIds = saleItemRepository.findTop3MostSoldProductIds();

        List<Product> products;
        if (!topIds.isEmpty()) {
            products = productRepository.findAllById(topIds);
        } else {
            products = productRepository.findTop3ByActiveTrueOrderByNameAsc();
        }

        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Transactional
    public ProductResponse save(ProductRequest request) {
        Category category = categoryService.findEntityById(request.categoryId());
        Provider provider = (request.providerId() != null) ? providerService.findById(request.providerId()) : null;

        Product product = productMapper.toEntity(request, category, provider);
        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        Category category = categoryService.findEntityById(request.categoryId());
        Provider provider = (request.providerId() != null) ? providerService.findById(request.providerId()) : null;

        productMapper.updateEntityFromRequest(request, existingProduct, category, provider);
        Product updateProduct = productRepository.save(existingProduct);
        return ProductResponse.fromEntity(updateProduct);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listProducts(String name, Long categoryId, String orderBy, int page,
            int size) {

        if ("mostSold".equalsIgnoreCase(orderBy) || "leastSold".equalsIgnoreCase(orderBy)) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productPage;

            if ("mostSold".equalsIgnoreCase(orderBy)) {
                productPage = productRepository.findAllByMostSold(name, categoryId, pageable);
            } else {
                productPage = productRepository.findAllByLeastSold(name, categoryId, pageable);
            }

            return new PageResponse<>(productPage.map(ProductResponse::fromEntity));
        }

        Sort sort = createSort(orderBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Product> spec = ProductSpecification.withFilters(name, categoryId);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return new PageResponse<>(productPage.map(ProductResponse::fromEntity));
    }

    /**
     * Finds a Product entity by its ID. This method is intended for internal use
     * by other services that need the raw entity.
     * 
     * @param id The ID of the product to find.
     * @return The found Product entity.
     * @throws ResourceNotFoundException if the product is not found.
     */
    @Transactional(readOnly = true)
    public Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = findEntityById(id);
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
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "name");
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "salePrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "salePrice");
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.ASC, "name"); // name_asc
        };
    }

}

package com.flick.business.api.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.flick.business.api.dto.request.ProductRequest;
import com.flick.business.api.dto.response.PageResponse;
import com.flick.business.api.dto.response.ProductResponse;
import com.flick.business.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            UriComponentsBuilder uriBuilder) {
        ProductResponse savedProduct = productService.save(request);
        URI uri = uriBuilder.path("/api/products/{id}").buildAndExpand(savedProduct.id()).toUri();
        return ResponseEntity.created(uri).body(savedProduct);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> listProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String orderBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<ProductResponse> products = productService.listProducts(name, categoryId, orderBy, page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findProductById(@PathVariable Long id) {
        ProductResponse product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse updatedProduct = productService.update(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @PostMapping("/{id}/copy")
    public ResponseEntity<ProductResponse> copyProduct(@PathVariable Long id, UriComponentsBuilder uriBuilder) {
        ProductResponse copiedProduct = productService.copyProduct(id);
        URI uri = uriBuilder.path("/api/products/{id}").buildAndExpand(copiedProduct.id()).toUri();
        return ResponseEntity.created(uri).body(copiedProduct);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleProductStatus(@PathVariable Long id) {
        productService.toggleActiveStatus(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> deleteProductPermanently(@PathVariable Long id) {
        productService.deletePermanently(id);
        return ResponseEntity.noContent().build();
    }

}

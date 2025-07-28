package com.flick.business.api.controller;

import com.flick.business.api.dto.request.production.CategoryRequest;
import com.flick.business.api.dto.response.production.CategoryResponse;
import com.flick.business.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listAllCategories() {
        List<CategoryResponse> categories = categoryService.listAll();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            UriComponentsBuilder uriBuilder) {
        CategoryResponse savedCategory = categoryService.save(request);
        URI uri = uriBuilder.path("/api/categories/{id}").buildAndExpand(savedCategory.id()).toUri();
        return ResponseEntity.created(uri).body(savedCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
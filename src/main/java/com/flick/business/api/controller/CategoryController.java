package com.flick.business.api.controller;

import com.flick.business.api.dto.request.CategoryRequest;
import com.flick.business.api.dto.response.CategoryResponse;
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
}
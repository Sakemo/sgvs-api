package com.flick.business.service;

import com.flick.business.api.dto.request.production.CategoryRequest;
import com.flick.business.api.dto.response.production.CategoryResponse;
import com.flick.business.core.entity.Category;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    public List<CategoryResponse> listAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CategoryResponse save(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(savedCategory);
    }
}
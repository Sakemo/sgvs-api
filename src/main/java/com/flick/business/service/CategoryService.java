package com.flick.business.service;

import com.flick.business.api.dto.request.production.CategoryRequest;
import com.flick.business.api.dto.response.production.CategoryResponse;
import com.flick.business.core.entity.Category;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.CategoryRepository;
import com.flick.business.repository.SaleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final SaleRepository saleRepository;

    public Category findEntityById(Long id) {
        return id != null ? categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id)) : null;
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

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category existingCategory = findEntityById(id);
        existingCategory.setName(request.name());
        Category updatedCategory = categoryRepository.save(existingCategory);
        return CategoryResponse.fromEntity(updatedCategory);
    }

    @Transactional
    public void delete(Long id) {
      if(id == null){
        return;
      }

      if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }

        if (saleRepository.countByCategoryId(id) > 0) {
            throw new BusinessException("Cannot delete category as it is currently associated with existing sales.");
        }

        categoryRepository.deleteById(id);
    }
}

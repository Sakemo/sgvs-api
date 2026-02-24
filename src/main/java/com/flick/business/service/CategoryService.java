package com.flick.business.service;

import com.flick.business.api.dto.request.production.CategoryRequest;
import com.flick.business.api.dto.response.production.CategoryResponse;
import com.flick.business.core.entity.Category;
import com.flick.business.core.entity.security.User;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.CategoryRepository;
import com.flick.business.repository.SaleRepository;
import com.flick.business.service.security.AuthenticatedUserService;

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
    private final AuthenticatedUserService authenticatedUserService;

    public Category findEntityById(Long id) {
      Long userId = authenticatedUserService.getAuthenticatedUserId();
      return id != null ? categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id)) : null;
    }

    public List<CategoryResponse> listAll() {
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        return categoryRepository.findByUserId(userId).stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CategoryResponse save(CategoryRequest request) {
        User currentUser = authenticatedUserService.getAuthenticatedUser();
        Category category = new Category();
        category.setUser(currentUser);
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

      Category category = findEntityById(id);

        if (saleRepository.countByCategoryId(id, authenticatedUserService.getAuthenticatedUserId()) > 0) {
            throw new BusinessException("Cannot delete category as it is currently associated with existing sales.");
        }

        categoryRepository.delete(category);
    }
}

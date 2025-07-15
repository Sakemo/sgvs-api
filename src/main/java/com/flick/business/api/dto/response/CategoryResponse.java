package com.flick.business.api.dto.response;

import com.flick.business.core.entity.Category;

public record CategoryResponse(Long id, String name) {
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
package com.wikt0r2115.library.controller.dto;

import com.wikt0r2115.library.domain.Category;

public record CategoryResponse(
        Long categoryId,
        String name
) {
    public static CategoryResponse from(Category category){
        return new CategoryResponse(category.getCategoryId(), category.getName());
    }
}

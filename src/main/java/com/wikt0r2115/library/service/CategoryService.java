package com.wikt0r2115.library.service;

import com.wikt0r2115.library.controller.dto.CategoryResponse;
import com.wikt0r2115.library.domain.Category;
import com.wikt0r2115.library.infrastructure.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(String name){
        return CategoryResponse.from(categoryRepository.save(new Category(name)));
    }

    public List<CategoryResponse> findAll(){
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}

package com.wikt0r2115.library.service;

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

    public Category createCategory(String name){
        return categoryRepository.save(new Category(name));
    }

    public List<Category> findAll(){
        return categoryRepository.findAll();
    }
}

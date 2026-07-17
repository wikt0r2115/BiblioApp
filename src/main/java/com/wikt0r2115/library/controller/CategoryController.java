package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createNewCategory(@Valid @RequestBody CreateCategoryRequest request){
        return CategoryResponse.from(categoryService.createCategory(request.name()));
    }
    @GetMapping
    public List<CategoryResponse>  findCategories(){
        return categoryService.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}

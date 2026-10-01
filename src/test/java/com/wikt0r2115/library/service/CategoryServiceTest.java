package com.wikt0r2115.library.service;

import com.wikt0r2115.library.controller.dto.CategoryResponse;
import com.wikt0r2115.library.domain.Category;
import com.wikt0r2115.library.infrastructure.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.wikt0r2115.library.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository);
    }

    @Test
    void createCategory_whenNameIsValid_savesNormalizedCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategoryWithId());

        CategoryResponse response = categoryService.createCategory(padded(CATEGORY_ONE));

        assertEquals(new CategoryResponse(CATEGORY_ID, CATEGORY_ONE), response);
        verify(categoryRepository).save(argThat(category -> CATEGORY_ONE.equals(category.getName())));
    }

    @Test
    void createCategory_whenNameIsInvalid_doesNotSave() {
        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory("  "));
        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(null));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void findAll_whenCategoriesExist_returnsMappedResponses() {
        Category second = new Category(CATEGORY_TWO);
        when(categoryRepository.findAll()).thenReturn(List.of(sampleCategoryWithId(), second));

        assertEquals(List.of(
                new CategoryResponse(CATEGORY_ID, CATEGORY_ONE),
                new CategoryResponse(null, CATEGORY_TWO)), categoryService.findAll());
    }

    @Test
    void findAll_whenNoCategoriesExist_returnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        assertEquals(List.of(), categoryService.findAll());
    }
}

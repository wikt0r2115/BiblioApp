package com.wikt0r2115.library.domain;

import org.junit.jupiter.api.Test;

import static com.wikt0r2115.library.TestData.CATEGORY_ONE;
import static com.wikt0r2115.library.TestData.padded;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryTest {
    @Test
    void create_whenNameIsValid_trimsName() {
        Category category = new Category(padded(CATEGORY_ONE));

        assertEquals(CATEGORY_ONE, category.getName());
    }

    @Test
    void create_whenNameIsNullOrBlank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Category(null));
        assertThrows(IllegalArgumentException.class, () -> new Category("  "));
    }

    @Test
    void create_whenNameExceeds255Characters_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Category("x".repeat(256)));
    }
}

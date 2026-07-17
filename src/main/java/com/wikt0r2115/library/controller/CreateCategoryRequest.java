package com.wikt0r2115.library.controller;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "Name must not be blank")
        String name
) {
}

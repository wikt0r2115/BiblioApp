package com.wikt0r2115.library.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CreateBookRequest(
        @NotBlank(message = "ISBN must not be blank")
        String isbn,

        @NotBlank(message = "Title must not be blank")
        String title,

        @Min(value = 1450, message = "Publication year must not be earlier than 1450")
        int publicationYear,

        @NotNull(message = "Author id must not be null")
        Long authorId,

        @NotEmpty(message = "Category ids must not be empty")
        Set<Long> categoryIds
) {
}

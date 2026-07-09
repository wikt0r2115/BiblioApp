package com.wikt0r2115.library.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateBookDetailsRequest(
        @NotBlank(message = "Title must not be blank")
        String title,

        @Min(value = 1450, message = "Publication year must not be earlier than 1450")
        int publicationYear,

        @NotBlank(message = "Author must not be blank")
        String author,

        @NotBlank(message = "Category must not be blank")
        String category
) {
}

package com.wikt0r2115.library.book.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeBookIsbnRequest(
        @NotBlank(message = "ISBN must not be blank")
        String isbn
) {
}

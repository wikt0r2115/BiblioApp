package com.wikt0r2115.library.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeBookIsbnRequest(
        @NotBlank(message = "ISBN must not be blank")
        String isbn
) {
}

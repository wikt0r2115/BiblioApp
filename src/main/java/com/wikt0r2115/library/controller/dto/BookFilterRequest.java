package com.wikt0r2115.library.controller.dto;

public record BookFilterRequest(
        Long authorId,
        String title,
        Boolean available
) {
}

package com.wikt0r2115.library.controller;

public record BookFilterRequest(
        Long authorId,
        String title,
        Boolean available
) {
}

package com.wikt0r2115.library.controller;

public record BookFilterRequest(
        String author,
        String title,
        Boolean available
) {
}

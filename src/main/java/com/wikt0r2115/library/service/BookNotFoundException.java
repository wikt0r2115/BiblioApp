package com.wikt0r2115.library.service;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("Book with "+id+" not found");
    }
}

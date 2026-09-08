package com.wikt0r2115.library.service;

public class BookAlreadyLoaned extends RuntimeException {
    public BookAlreadyLoaned(Long id) {
        super("Book with id "+id+" is already loaned");
    }
}

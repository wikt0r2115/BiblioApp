package com.wikt0r2115.library.service.exception;

public class BookAlreadyLoanedException extends RuntimeException {
    public BookAlreadyLoanedException(Long id) {
        super("Book with id "+id+" is already loaned");
    }
}

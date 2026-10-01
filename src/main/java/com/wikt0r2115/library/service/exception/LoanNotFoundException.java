package com.wikt0r2115.library.service.exception;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(Long id) {
        super("Loan with " + id + " not found");
    }
}

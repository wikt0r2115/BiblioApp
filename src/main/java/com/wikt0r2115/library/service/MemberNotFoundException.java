package com.wikt0r2115.library.service;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long id) {
        super("Member with " + id + " not found");
    }
}

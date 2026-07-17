package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.domain.Author;

public record AuthorResponse(
        Long id,
        String firstName,
        String lastName
) {
    public static AuthorResponse from(Author author){
        return new AuthorResponse(
                author.getAuthorId(),
                author.getFirstName(),
                author.getLastName()
        );
    }
}

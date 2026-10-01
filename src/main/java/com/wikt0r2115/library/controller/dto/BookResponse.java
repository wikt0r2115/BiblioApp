package com.wikt0r2115.library.controller.dto;

import com.wikt0r2115.library.domain.Book;

import java.util.Set;
import java.util.stream.Collectors;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        int publicationYear,
        boolean available,
        AuthorResponse author,
        Set<CategoryResponse> categories
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getBookId(),
                book.getIsbn(),
                book.getTitle(),
                book.getPublicationYear(),
                book.isAvailable(),
                AuthorResponse.from(book.getAuthor()),
                book.getCategories().stream()
                        .map(CategoryResponse::from)
                        .collect(Collectors.toSet())
        );
    }
}

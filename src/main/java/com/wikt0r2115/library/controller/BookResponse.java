package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.domain.Book;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        int publicationYear,
        boolean available,
        String author,
        String category
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getBookId(),
                book.getIsbn(),
                book.getTitle(),
                book.getPublicationYear(),
                book.isAvailable(),
                book.getAuthor(),
                book.getCategory()
        );
    }
}

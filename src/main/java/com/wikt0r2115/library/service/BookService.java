package com.wikt0r2115.library.service;

import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.infrastructure.BookRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(String isbn, String title, int publicationYear, String author, String category){
        Book book = new Book(isbn, title, publicationYear, author, category);
        return bookRepository.save(book);
    }

    public Book findById(Long id){
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Page<Book> findAll(String author, String title, Boolean available, Pageable pageable){
        return bookRepository.findWithFilters(
                normalizeTextFilter(author),
                normalizeTextFilter(title),
                available,
                pageable);
    }

    public Book updateDetails(Long id, String title, int publicationYear, String author, String category){
        Book book = findById(id);
        book.updateDetails(title, publicationYear, author, category);
        return bookRepository.save(book);
    }

    public Book changeIsbn(Long id, String isbn){
        Book book = findById(id);
        book.changeIsbn(isbn);
        return bookRepository.save(book);
    }

    public Book markBorrowed(Long id){
        Book book = findById(id);
        book.markBorrowed();
        return bookRepository.save(book);
    }

    public Book markReturned(Long id){
        Book book = findById(id);
        book.markReturned();
        return bookRepository.save(book);
    }

    public Book deleteBook(Long id){
        Book book = findById(id);
        bookRepository.delete(book);
        return book;
    }

    private String normalizeTextFilter(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}

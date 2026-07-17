package com.wikt0r2115.library.service;

import com.wikt0r2115.library.domain.Author;
import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Category;
import com.wikt0r2115.library.infrastructure.AuthorRepository;
import com.wikt0r2115.library.infrastructure.BookRepository;
import com.wikt0r2115.library.infrastructure.CategoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    public Book createBook(String isbn, String title, int publicationYear, Long authorId, Set<Long> categoryIds){
        Author author = findAuthor(authorId);
        Set<Category> categories = findCategories(categoryIds);
        Book book = new Book(isbn, title, publicationYear, author, categories);
        return bookRepository.save(book);
    }

    public Book findById(Long id){
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Page<Book> findAll(Long authorId, String title, Boolean available, Pageable pageable){
        return bookRepository.findWithFilters(
                authorId,
                normalizeTextFilter(title),
                available,
                pageable);
    }

    public Book updateDetails(Long id, String title, int publicationYear, Long authorId, Set<Long> categoryIds){
        Book book = findById(id);
        Author author = findAuthor(authorId);
        Set<Category> categories = findCategories(categoryIds);

        book.updateDetails(title, publicationYear, author, categories);
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

    public void deleteBook(Long id){
        Book book = findById(id);
        bookRepository.delete(book);
    }

    private String normalizeTextFilter(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private Author findAuthor(Long authorId) {
        if(authorId == null)
            throw new IllegalArgumentException("Author id must not be null");

        return authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author with id " + authorId + " does not exist"));
    }

    private Set<Category> findCategories(Set<Long> categoryIds) {
        if(categoryIds == null || categoryIds.isEmpty())
            throw new IllegalArgumentException("Category ids must not be empty");

        if(categoryIds.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Category ids must not contain null");

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        if(categories.size() != categoryIds.size())
            throw new IllegalArgumentException("One or more categories do not exist");

        return categories;
    }
}

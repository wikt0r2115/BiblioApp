package com.wikt0r2115.library.service;

import com.wikt0r2115.library.controller.dto.BookResponse;
import com.wikt0r2115.library.controller.dto.PageResponse;
import com.wikt0r2115.library.domain.Author;
import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Category;
import com.wikt0r2115.library.infrastructure.AuthorRepository;
import com.wikt0r2115.library.infrastructure.BookRepository;
import com.wikt0r2115.library.infrastructure.CategoryRepository;
import com.wikt0r2115.library.infrastructure.LoanRepository;
import com.wikt0r2115.library.service.exception.AuthorNotFoundException;
import com.wikt0r2115.library.service.exception.BookNotFoundException;
import com.wikt0r2115.library.service.exception.CategoryNotFoundException;
import jakarta.transaction.Transactional;
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
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       CategoryRepository categoryRepository,
                       LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.loanRepository = loanRepository;
    }

    public BookResponse createBook(String isbn, String title, int publicationYear, Long authorId, Set<Long> categoryIds){
        Author author = findAuthor(authorId);
        Set<Category> categories = findCategories(categoryIds);
        Book book = new Book(isbn, title, publicationYear, author, categories);
        return BookResponse.from(bookRepository.save(book));
    }

    public Book findById(Long id){
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public BookResponse findByIdWhereArchivedFalse(Long id){
        return BookResponse.from(findActiveBook(id));
    }

    private Book findActiveBook(Long id){
        return bookRepository.findByIdWhereArchivedFalse(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public PageResponse<BookResponse> findAll(Long authorId, String title, Boolean available, Pageable pageable){
        Set<String> allowedFields = Set.of("bookId", "title", "publicationYear", "available");

        for(var order : pageable.getSort()) {
            if(!allowedFields.contains(order.getProperty()))
                throw new IllegalArgumentException("Unsupported sort field "+order.getProperty());
        }

        Page<BookResponse> page = bookRepository.findWithFilters(
                authorId,
                normalizeTextFilter(title),
                available,
                pageable).map(BookResponse::from);
        return PageResponse.from(page);
    }

    public BookResponse updateDetails(Long id, String title, int publicationYear, Long authorId, Set<Long> categoryIds){
        Book book = findActiveBook(id);
        Author author = findAuthor(authorId);
        Set<Category> categories = findCategories(categoryIds);

        book.updateDetails(title, publicationYear, author, categories);
        return BookResponse.from(bookRepository.save(book));
    }

    public BookResponse changeIsbn(Long id, String isbn){
        Book book = findActiveBook(id);
        book.changeIsbn(isbn);
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Long id){
        Book book = findActiveBook(id);
        if(loanRepository.findByBookAndReturnedAtIsNull(book).isPresent())
            throw new IllegalStateException("Book is loaned");
        book.markArchived();
        bookRepository.save(book);
    }

    private String normalizeTextFilter(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private Author findAuthor(Long authorId) {
        if(authorId == null)
            throw new IllegalArgumentException("Author id must not be null");

        return authorRepository.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException("Author with id " + authorId + " does not exist"));
    }

    private Set<Category> findCategories(Set<Long> categoryIds) {
        if(categoryIds == null || categoryIds.isEmpty())
            throw new IllegalArgumentException("Category ids must not be empty");

        if(categoryIds.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Category ids must not contain null");

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        if(categories.size() != categoryIds.size())
            throw new CategoryNotFoundException("One or more categories do not exist");

        return categories;
    }
}

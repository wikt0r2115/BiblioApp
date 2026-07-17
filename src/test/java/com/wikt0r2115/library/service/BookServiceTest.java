package com.wikt0r2115.library.service;

import com.wikt0r2115.library.domain.Author;
import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Category;
import com.wikt0r2115.library.infrastructure.AuthorRepository;
import com.wikt0r2115.library.infrastructure.BookRepository;
import com.wikt0r2115.library.infrastructure.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private BookService bookService;

    private final Long BOOK_ID = 1L;
    private final Long AUTHOR_ID = 2L;
    private final Long NEW_AUTHOR_ID = 3L;
    private final Long CATEGORY_ID = 4L;
    private final Long SECOND_CATEGORY_ID = 5L;
    private final Long NEW_CATEGORY_ID = 6L;
    private final Long SECOND_NEW_CATEGORY_ID = 7L;
    private final String ISBN = "152935112X";
    private final String TITLE = "Atomic Habits";
    private final int PUBLICATION_YEAR = 2005;
    private final Author AUTHOR = new Author("James", "Clear");
    private final Author NEW_AUTHOR = new Author("Robert", "Martin");
    private final Category CATEGORY = new Category("Psychology");
    private final Category SECOND_CATEGORY = new Category("Science");
    private final Category NEW_CATEGORY = new Category("Programming");
    private final Category SECOND_NEW_CATEGORY = new Category("Craft");
    private final Set<Long> CATEGORY_IDS = Set.of(CATEGORY_ID, SECOND_CATEGORY_ID);
    private final Set<Long> NEW_CATEGORY_IDS = Set.of(NEW_CATEGORY_ID, SECOND_NEW_CATEGORY_ID);
    private final Set<Category> CATEGORIES = Set.of(CATEGORY, SECOND_CATEGORY);
    private final Set<Category> NEW_CATEGORIES = Set.of(NEW_CATEGORY, SECOND_NEW_CATEGORY);
    private final String NEW_ISBN = "9780136091813";
    private final String NEW_TITLE = "TITLE";
    private final int NEW_PUBLICATION_YEAR = 2018;

    @BeforeEach
    void setUp(){
        bookService = new BookService(bookRepository, authorRepository, categoryRepository);
    }

    @Test
    public void createBook_savesBook(){
        givenExistingAuthorAndCategories(AUTHOR_ID, AUTHOR, CATEGORY_IDS, CATEGORIES);
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Book book = bookService.createBook(ISBN, TITLE, PUBLICATION_YEAR, AUTHOR_ID, CATEGORY_IDS);

        assertEquals(ISBN, book.getIsbn());
        assertEquals(TITLE, book.getTitle());
        assertEquals(PUBLICATION_YEAR, book.getPublicationYear());
        assertSame(AUTHOR, book.getAuthor());
        assertEquals(CATEGORIES, book.getCategories());
        assertTrue(book.isAvailable());

        verify(bookRepository).save(book);
    }

    @Test
    public void createBook_whenAuthorDoesntExist_throwsIllegalArgumentException(){
        when(authorRepository.findById(AUTHOR_ID))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> bookService.createBook(ISBN, TITLE, PUBLICATION_YEAR, AUTHOR_ID, CATEGORY_IDS));
    }

    @Test
    public void createBook_whenCategoryDoesntExist_throwsIllegalArgumentException(){
        when(authorRepository.findById(AUTHOR_ID))
                .thenReturn(Optional.of(AUTHOR));
        when(categoryRepository.findAllById(CATEGORY_IDS))
                .thenReturn(List.of(CATEGORY));

        assertThrows(IllegalArgumentException.class,
                () -> bookService.createBook(ISBN, TITLE, PUBLICATION_YEAR, AUTHOR_ID, CATEGORY_IDS));
    }

    @Test
    public void findById_whenBookExists_returnsBook(){
        Book book = sampleBook();
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.of(book));
        Book result = bookService.findById(BOOK_ID);
        assertEquals(book, result);
    }

    @Test
    public void findById_whenBookDoesntExist_throwsBookNotFoundException(){
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> bookService.findById(BOOK_ID));
    }

    @Test
    public void findAll_withFilters_returnsFilteredPage(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> expectedPage = new PageImpl<>(Collections.emptyList());

        when(bookRepository.findWithFilters(AUTHOR_ID, TITLE, true, pageable))
                .thenReturn(expectedPage);

        Page<Book> result = bookService.findAll(AUTHOR_ID, "  " + TITLE + "  ", true, pageable);

        assertSame(expectedPage, result);
        verify(bookRepository).findWithFilters(AUTHOR_ID, TITLE, true, pageable);
    }

    @Test
    public void updateDetails_whenBookExists_updatesAndSavesBook(){
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.of(sampleBook()));
        givenExistingAuthorAndCategories(NEW_AUTHOR_ID, NEW_AUTHOR, NEW_CATEGORY_IDS, NEW_CATEGORIES);
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Book book = bookService.updateDetails(
                BOOK_ID,
                NEW_TITLE,
                NEW_PUBLICATION_YEAR,
                NEW_AUTHOR_ID,
                NEW_CATEGORY_IDS);

        assertEquals(NEW_TITLE, book.getTitle());
        assertEquals(NEW_PUBLICATION_YEAR, book.getPublicationYear());
        assertSame(NEW_AUTHOR, book.getAuthor());
        assertEquals(NEW_CATEGORIES, book.getCategories());

        verify(bookRepository).save(book);
    }

    @Test
    public void changeIsbn_whenBookExists_updatesAndSavesBook(){
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.of(sampleBook()));
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Book book = bookService.changeIsbn(BOOK_ID, NEW_ISBN);
        assertEquals(NEW_ISBN, book.getIsbn());
        verify(bookRepository).save(book);
    }

    @Test
    public void markBorrowed_whenBookExists_marksAsBorrowedAndSavesBook(){
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.of(sampleBook()));
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Book book = bookService.markBorrowed(BOOK_ID);
        assertFalse(book.isAvailable());
        verify(bookRepository).save(book);
    }

    @Test
    public void markReturned_whenBookExists_marksAsReturnedAndSavesBook(){
        Book borrowedBook = sampleBook();
        borrowedBook.markBorrowed();
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.of(borrowedBook));
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Book book = bookService.markReturned(BOOK_ID);

        assertTrue(book.isAvailable());
        verify(bookRepository).save(book);
    }

    @Test
    public void deleteBook_whenBookExists_deletesBook(){
        Book existingBook = sampleBook();
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.of(existingBook));

        bookService.deleteBook(BOOK_ID);

        verify(bookRepository).delete(existingBook);
    }

    private Book sampleBook(){
        return new Book(ISBN, TITLE, PUBLICATION_YEAR, AUTHOR, CATEGORIES);
    }

    private void givenExistingAuthorAndCategories(
            Long authorId,
            Author author,
            Set<Long> categoryIds,
            Set<Category> categories){
        when(authorRepository.findById(authorId))
                .thenReturn(Optional.of(author));
        when(categoryRepository.findAllById(categoryIds))
                .thenReturn(List.copyOf(categories));
    }
}

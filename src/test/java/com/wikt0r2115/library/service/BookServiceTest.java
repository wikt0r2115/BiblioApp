package com.wikt0r2115.library.service;

import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.infrastructure.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    private final Long BOOK_ID = 1L;
    private final String ISBN = "152935112X";
    private final String TITLE = "Atomic Habits";
    private final int PUBLICATION_YEAR = 2005;
    private final String AUTHOR = "James Clear";
    private final String CATEGORY = "Psychology";
    private final String NEW_ISBN = "9780136091813";
    private final String NEW_TITLE = "TITLE";
    private final int NEW_PUBLICATION_YEAR = 2018;
    private final String NEW_AUTHOR = "AUTHOR";
    private final String NEW_CATEGORY = "CATEGORY";

    @BeforeEach
    void setUp(){
        bookService = new BookService(bookRepository);
    }

    @Test
    public void createBook_savesBook(){
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Book book = bookService.createBook(ISBN, TITLE, PUBLICATION_YEAR, AUTHOR, CATEGORY);

        assertEquals(ISBN, book.getIsbn());
        assertEquals(TITLE, book.getTitle());
        assertEquals(PUBLICATION_YEAR, book.getPublicationYear());
        assertEquals(AUTHOR, book.getAuthor());
        assertEquals(CATEGORY, book.getCategory());
        assertTrue(book.isAvailable());

        verify(bookRepository).save(book);
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

        when(bookRepository.findWithFilters(AUTHOR, TITLE, true, pageable))
                .thenReturn(expectedPage);

        Page<Book> result = bookService.findAll("  " + AUTHOR + "  ", "  " + TITLE + "  ", true, pageable);

        assertSame(expectedPage, result);
        verify(bookRepository).findWithFilters(AUTHOR, TITLE, true, pageable);
    }

    @Test
    public void updateDetails_whenBookExists_updatesAndSavesBook(){
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.of(sampleBook()));
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Book book = bookService.updateDetails(BOOK_ID, NEW_TITLE, NEW_PUBLICATION_YEAR, NEW_AUTHOR, NEW_CATEGORY);
        assertEquals(NEW_TITLE, book.getTitle());
        assertEquals(NEW_PUBLICATION_YEAR, book.getPublicationYear());
        assertEquals(NEW_AUTHOR, book.getAuthor());
        assertEquals(NEW_CATEGORY, book.getCategory());

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
    public void deleteBook_whenBookExists_deletesAndReturnsBook(){
        Book existingBook = sampleBook();
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.of(existingBook));

        Book book = bookService.deleteBook(BOOK_ID);

        assertSame(existingBook, book);
        verify(bookRepository).delete(book);
    }

    private Book sampleBook(){
        return new Book(ISBN, TITLE, PUBLICATION_YEAR, AUTHOR, CATEGORY);
    }
}

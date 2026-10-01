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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.wikt0r2115.library.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LoanRepository loanRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, authorRepository, categoryRepository, loanRepository);
    }

    @Test
    void createBook_savesBook() {
        Author author = sampleAuthor();
        Set<Category> categories = sampleCategories();
        givenExistingAuthorAndCategories(AUTHOR_ID, author, CATEGORY_IDS, categories);
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse book = bookService.createBook(
                NORMALIZED_VALID_ISBN10, BOOK_TITLE, PUBLICATION_YEAR, AUTHOR_ID, CATEGORY_IDS);

        assertEquals(NORMALIZED_VALID_ISBN10, book.isbn());
        assertEquals(BOOK_TITLE, book.title());
        assertEquals(PUBLICATION_YEAR, book.publicationYear());
        assertEquals(author.getFirstName(), book.author().firstName());
        assertEquals(categories.size(), book.categories().size());
        assertTrue(book.available());

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_whenAuthorDoesntExist_throwsAuthorNotFoundException() {
        when(authorRepository.findById(AUTHOR_ID))
                .thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class,
                () -> bookService.createBook(
                        NORMALIZED_VALID_ISBN10, BOOK_TITLE, PUBLICATION_YEAR, AUTHOR_ID, CATEGORY_IDS));
    }

    @Test
    void createBook_whenCategoryDoesntExist_throwsCategoryNotFoundException() {
        when(authorRepository.findById(AUTHOR_ID))
                .thenReturn(Optional.of(sampleAuthor()));
        when(categoryRepository.findAllById(CATEGORY_IDS))
                .thenReturn(List.of(sampleCategory()));

        assertThrows(CategoryNotFoundException.class,
                () -> bookService.createBook(
                        NORMALIZED_VALID_ISBN10, BOOK_TITLE, PUBLICATION_YEAR, AUTHOR_ID, CATEGORY_IDS));
    }

    @Test
    void findById_whenBookExists_returnsBook() {
        Book book = sampleBookWithIsbn10();
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.of(book));
        Book result = bookService.findById(BOOK_ID);
        assertSame(book, result);
    }

    @Test
    void findById_whenBookDoesntExist_throwsBookNotFoundException() {
        when(bookRepository.findById(BOOK_ID))
                .thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> bookService.findById(BOOK_ID));
    }

    @Test
    void findAll_withFilters_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> expectedPage = new PageImpl<>(Collections.emptyList());

        when(bookRepository.findWithFilters(AUTHOR_ID, BOOK_TITLE, true, pageable))
                .thenReturn(expectedPage);

        PageResponse<BookResponse> result = bookService.findAll(
                AUTHOR_ID, "  " + BOOK_TITLE + "  ", true, pageable);

        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(bookRepository).findWithFilters(AUTHOR_ID, BOOK_TITLE, true, pageable);
    }

    @Test
    void findAll_whenPageContainsBooks_returnsMappedResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> expectedPage = new PageImpl<>(List.of(sampleBookWithIsbn10()));

        when(bookRepository.findWithFilters(AUTHOR_ID, BOOK_TITLE, true, pageable))
                .thenReturn(expectedPage);

        PageResponse<BookResponse> result = bookService.findAll(
                AUTHOR_ID, "  " + BOOK_TITLE + "  ", true, pageable);

        assertEquals(1, result.content().size());
        assertEquals(NORMALIZED_VALID_ISBN10, result.content().getFirst().isbn());
        assertEquals(BOOK_TITLE, result.content().getFirst().title());
    }

    @Test
    void updateDetails_whenBookExists_updatesAndSavesBook() {
        Author newAuthor = sampleNewAuthor();
        Set<Category> newCategories = sampleNewCategories();
        Book existingBook = sampleBookWithIsbn10();
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID))
                .thenReturn(Optional.of(existingBook));
        givenExistingAuthorAndCategories(
                NEW_AUTHOR_ID, newAuthor, NEW_CATEGORY_IDS, newCategories);
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse book = bookService.updateDetails(
                BOOK_ID,
                NEW_BOOK_TITLE,
                NEW_PUBLICATION_YEAR,
                NEW_AUTHOR_ID,
                NEW_CATEGORY_IDS);

        assertEquals(NEW_BOOK_TITLE, book.title());
        assertEquals(NEW_PUBLICATION_YEAR, book.publicationYear());
        assertEquals(newAuthor.getFirstName(), book.author().firstName());
        assertEquals(newCategories.size(), book.categories().size());

        verify(bookRepository).save(existingBook);
    }

    @Test
    void changeIsbn_whenBookExists_updatesAndSavesBook() {
        Book existingBook = sampleBookWithIsbn10();
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID))
                .thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        BookResponse book = bookService.changeIsbn(BOOK_ID, NEW_ISBN);
        assertEquals(NEW_ISBN, book.isbn());
        verify(bookRepository).save(existingBook);
    }

    @Test
    void deleteBook_whenBookExists_archivesBook() {
        Book existingBook = sampleBookWithIsbn10();
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID))
                .thenReturn(Optional.of(existingBook));

        bookService.deleteBook(BOOK_ID);

        assertTrue(existingBook.isArchived());
        verify(bookRepository).save(existingBook);
    }

    @Test
    void findAll_whenSortFieldIsUnsupported_throwsIllegalArgumentException() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("doesNotExist"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookService.findAll(null, null, null, pageable));

        assertEquals("Unsupported sort field doesNotExist", exception.getMessage());
        verifyNoInteractions(bookRepository);
    }

    @Test
    void findByIdWhereArchivedFalse_whenBookExists_returnsMappedResponse() {
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID))
                .thenReturn(Optional.of(sampleBookWithId()));

        BookResponse response = bookService.findByIdWhereArchivedFalse(BOOK_ID);

        assertEquals(BOOK_ID, response.id());
        assertEquals(NORMALIZED_VALID_ISBN13, response.isbn());
    }

    @Test
    void findByIdWhereArchivedFalse_whenBookIsMissing_throwsBookNotFoundException() {
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.findByIdWhereArchivedFalse(BOOK_ID));
    }

    @Test
    void createBook_whenAuthorIdIsNull_doesNotSave() {
        assertThrows(IllegalArgumentException.class,
                () -> bookService.createBook(VALID_ISBN13, BOOK_TITLE, PUBLICATION_YEAR, null, CATEGORY_IDS));
        verifyNoInteractions(bookRepository, authorRepository, categoryRepository);
    }

    @Test
    void createBook_whenCategoryIdsAreInvalid_doesNotSave() {
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(sampleAuthor()));

        assertThrows(IllegalArgumentException.class,
                () -> bookService.createBook(VALID_ISBN13, BOOK_TITLE, PUBLICATION_YEAR, AUTHOR_ID, null));
        assertThrows(IllegalArgumentException.class,
                () -> bookService.createBook(VALID_ISBN13, BOOK_TITLE, PUBLICATION_YEAR, AUTHOR_ID, Set.of()));
        verifyNoInteractions(bookRepository, categoryRepository);
    }

    @Test
    void findAll_whenTitleIsBlank_passesNullFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findWithFilters(null, null, null, pageable))
                .thenReturn(Page.empty(pageable));

        PageResponse<BookResponse> response = bookService.findAll(null, "   ", null, pageable);

        assertTrue(response.content().isEmpty());
        assertEquals(0, response.totalElements());
        verify(bookRepository).findWithFilters(null, null, null, pageable);
    }

    @Test
    void updateDetails_whenBookIsMissing_doesNotSave() {
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.updateDetails(BOOK_ID, NEW_BOOK_TITLE, NEW_PUBLICATION_YEAR,
                        NEW_AUTHOR_ID, NEW_CATEGORY_IDS));
        verifyNoInteractions(authorRepository, categoryRepository);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateDetails_whenAuthorIsMissing_doesNotSave() {
        Book existingBook = sampleBookWithId();
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.of(existingBook));
        when(authorRepository.findById(NEW_AUTHOR_ID)).thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class,
                () -> bookService.updateDetails(BOOK_ID, NEW_BOOK_TITLE, NEW_PUBLICATION_YEAR,
                        NEW_AUTHOR_ID, NEW_CATEGORY_IDS));
        assertEquals(BOOK_TITLE, existingBook.getTitle());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateDetails_whenCategoryIsMissing_doesNotSave() {
        Book existingBook = sampleBookWithId();
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.of(existingBook));
        when(authorRepository.findById(NEW_AUTHOR_ID)).thenReturn(Optional.of(sampleNewAuthor()));
        when(categoryRepository.findAllById(NEW_CATEGORY_IDS)).thenReturn(List.of(sampleCategory()));

        assertThrows(CategoryNotFoundException.class,
                () -> bookService.updateDetails(BOOK_ID, NEW_BOOK_TITLE, NEW_PUBLICATION_YEAR,
                        NEW_AUTHOR_ID, NEW_CATEGORY_IDS));
        assertEquals(BOOK_TITLE, existingBook.getTitle());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void changeIsbn_whenBookIsMissing_throwsBookNotFoundException() {
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.changeIsbn(BOOK_ID, NEW_ISBN));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void changeIsbn_whenIsbnIsInvalid_preservesOriginalValue() {
        Book existingBook = sampleBookWithId();
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.of(existingBook));

        assertThrows(IllegalArgumentException.class,
                () -> bookService.changeIsbn(BOOK_ID, "invalid"));
        assertEquals(NORMALIZED_VALID_ISBN13, existingBook.getIsbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_whenActiveLoanExists_doesNotArchive() {
        Book existingBook = sampleBookWithId();
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.of(existingBook));
        when(loanRepository.findByBookAndReturnedAtIsNull(existingBook))
                .thenReturn(Optional.of(sampleActiveLoanWithIds()));

        assertThrows(IllegalStateException.class, () -> bookService.deleteBook(BOOK_ID));
        assertFalse(existingBook.isArchived());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_whenBookIsMissing_doesNotSave() {
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(BOOK_ID));
        verifyNoInteractions(loanRepository);
        verify(bookRepository, never()).save(any(Book.class));
    }

    private void givenExistingAuthorAndCategories(
            Long authorId,
            Author author,
            Set<Long> categoryIds,
            Set<Category> categories) {
        when(authorRepository.findById(authorId))
                .thenReturn(Optional.of(author));
        when(categoryRepository.findAllById(categoryIds))
                .thenReturn(List.copyOf(categories));
    }
}

package com.wikt0r2115.library.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static com.wikt0r2115.library.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

public class BookTest {
    @Test
    void create_validBook(){
        Book book = sampleBook();
        assertAll(
                () -> assertNull(book.getBookId()),
                () -> assertEquals(NORMALIZED_VALID_ISBN13, book.getIsbn()),
                () -> assertEquals(BOOK_TITLE, book.getTitle()),
                () -> assertEquals(PUBLICATION_YEAR, book.getPublicationYear()),
                () -> assertTrue(book.isAvailable()),
                () -> assertAuthor(book.getAuthor(), AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME),
                () -> assertCategoryNames(book.getCategories(), CATEGORY_ONE, CATEGORY_TWO)
        );
    }

    @Test
    void create_trims_normalize_Data_ISBN13(){
        Book book = new Book(
                VALID_ISBN13,
                padded(BOOK_TITLE),
                PUBLICATION_YEAR,
                new Author(padded(AUTHOR_FIRST_NAME), padded(AUTHOR_LAST_NAME)),
                Set.of(
                        new Category(padded(CATEGORY_ONE)),
                        new Category(padded(CATEGORY_TWO))
                ));

        assertEquals(NORMALIZED_VALID_ISBN13, book.getIsbn());
        assertEquals(BOOK_TITLE, book.getTitle());
        assertAuthor(book.getAuthor(), AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME);
        assertCategoryNames(book.getCategories(), CATEGORY_ONE, CATEGORY_TWO);
    }

    @Test
    void create_trims_normalize_Data_ISBN10(){
        Book book = new Book(
                VALID_ISBN10,
                padded(BOOK_TITLE),
                PUBLICATION_YEAR,
                new Author(padded(AUTHOR_FIRST_NAME), padded(AUTHOR_LAST_NAME)),
                Set.of(
                        new Category(padded(CATEGORY_ONE)),
                        new Category(padded(CATEGORY_TWO))
                ));

        assertEquals(NORMALIZED_VALID_ISBN10, book.getIsbn());
        assertEquals(BOOK_TITLE, book.getTitle());
        assertAuthor(book.getAuthor(), AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME);
        assertCategoryNames(book.getCategories(), CATEGORY_ONE, CATEGORY_TWO);
    }

    @Test
    void create_whenISBNisNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(null, BOOK_TITLE, PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
    }

    @Test
    void create_whenTitleIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, null, PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, "", PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
    }

    @Test
    void create_whenPublicationYearIsLowerThan1450ORHigherThanCurrentYear_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, BOOK_TITLE, 1300, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, BOOK_TITLE, 2300, sampleAuthor(), sampleCategories()));
    }

    @Test
    void create_whenAuthorIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, BOOK_TITLE, PUBLICATION_YEAR, null, sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, BOOK_TITLE, PUBLICATION_YEAR, new Author("", ""), sampleCategories()));
    }

    @Test
    void create_whenCategoryIsNullOrEmptyOrContainsNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, BOOK_TITLE, PUBLICATION_YEAR, sampleAuthor(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, BOOK_TITLE, PUBLICATION_YEAR, sampleAuthor(), Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, BOOK_TITLE, PUBLICATION_YEAR, sampleAuthor(), categoriesWithNull()));
    }

    @Test
    void create_whenInvalidISBN_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book("1529351125", BOOK_TITLE, PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> new Book("3213213213213", BOOK_TITLE, PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
    }

    @Test
    void markBorrowed_changesAvailabilityToFalse(){
        Book book = sampleBook();
        book.markBorrowed();
        assertFalse(book.isAvailable());
    }

    @Test
    void markBorrowed_whenBookIsNotAvailable_throwsIllegalStateException(){
        Book book = sampleBook();
        book.markBorrowed();
        assertThrows(IllegalStateException.class, book::markBorrowed);
    }

    @Test
    void markReturned_changesAvailabilityToTrue(){
        Book book = sampleBook();
        book.markBorrowed();
        book.markReturned();
        assertTrue(book.isAvailable());
    }

    @Test
    void markReturned_whenBookIsAvailable_throwsIllegalStateException(){
        Book book = sampleBook();
        assertThrows(IllegalStateException.class, book::markReturned);
    }

    @Test
    void updateDetails_whenValidData_changesData(){
        Book book = sampleBook();
        String oldIsbn = book.getIsbn();
        boolean oldAvailability = book.isAvailable();
        Author newAuthor = new Author("Robert", "Martin");
        Set<Category> newCategories = Set.of(new Category("Programming"), new Category("Craft"));

        book.updateDetails("Clean Code", 2008, newAuthor, newCategories);

        assertEquals("Clean Code", book.getTitle());
        assertEquals(2008, book.getPublicationYear());
        assertSame(newAuthor, book.getAuthor());
        assertSame(newCategories, book.getCategories());
        assertEquals(oldIsbn, book.getIsbn());
        assertEquals(oldAvailability, book.isAvailable());
    }

    @Test
    void updateDetails_whenInvalidData_throwsIllegalArgumentException(){
        Book book = sampleBook();
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails("", PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(null, PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(BOOK_TITLE, 1300, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(BOOK_TITLE, 2060, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(BOOK_TITLE, PUBLICATION_YEAR, null, sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(BOOK_TITLE, PUBLICATION_YEAR, new Author("", ""), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(BOOK_TITLE, PUBLICATION_YEAR, sampleAuthor(), Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(BOOK_TITLE, PUBLICATION_YEAR, sampleAuthor(), null));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(BOOK_TITLE, PUBLICATION_YEAR, sampleAuthor(), categoriesWithNull()));
    }

    @Test
    void changeIsbn_whenValidIsbn13_changesIsbn(){
        Book book = sampleBook();
        book.changeIsbn("978-0-13609181-3");
        assertEquals("9780136091813", book.getIsbn());
    }

    @Test
    void changeIsbn_whenValidIsbn10_changesIsbn(){
        Book book = sampleBook();
        book.changeIsbn(VALID_ISBN10);
        assertEquals(NORMALIZED_VALID_ISBN10, book.getIsbn());
    }

    @Test
    void changeIsbn_whenxinIsbn10_normalizeIsbn(){
        Book book = sampleBook();
        book.changeIsbn("152935112x");
        assertEquals(NORMALIZED_VALID_ISBN10, book.getIsbn());
    }

    @Test
    void changeIsbn_whenInvalidIsbn13_ThrowsIllegalArgumentException(){
        Book book = sampleBook();
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn(""));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn(null));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("974-1-60309-502-0"));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("978-1-60309-502-1"));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("978-1-60309-502-X"));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("978-1-60309-5022-X"));
    }

    @Test
    void changeIsbn_whenInvalidIsbn10_ThrowsIllegalArgumentException(){
        Book book = sampleBook();
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn(""));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn(null));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("152935112A"));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("1529351112X"));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("1G2935112A"));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("152935113X"));
    }

    private void assertAuthor(Author author, String firstName, String lastName){
        assertEquals(firstName, author.getFirstName());
        assertEquals(lastName, author.getLastName());
    }

    private void assertCategoryNames(Set<Category> categories, String... expectedNames){
        Set<String> actualNames = categories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of(expectedNames), actualNames);
    }
}

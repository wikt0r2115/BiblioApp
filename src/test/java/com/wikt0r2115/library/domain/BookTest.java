package com.wikt0r2115.library.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {
    private static final String VALID_ISBN13 = "978-1-60309-502-0";
    private static final String VALID_ISBN10 = "152935112x";
    private static final String NORMALIZED_VALID_ISBN13 = "9781603095020";
    private static final String NORMALIZED_VALID_ISBN10 = "152935112X";
    private static final String TITLE = "Atomic Habits";
    private static final int PUBLICATION_YEAR = 2005;
    private static final String AUTHOR_FIRST_NAME = "James";
    private static final String AUTHOR_LAST_NAME = "Clear";

    @Test
    void create_validBook(){
        Book book = sampleBook();
        assertAll(
                () -> assertNull(book.getBookId()),
                () -> assertEquals(NORMALIZED_VALID_ISBN13, book.getIsbn()),
                () -> assertEquals(TITLE, book.getTitle()),
                () -> assertEquals(PUBLICATION_YEAR, book.getPublicationYear()),
                () -> assertTrue(book.isAvailable()),
                () -> assertAuthor(book.getAuthor(), AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME),
                () -> assertCategoryNames(book.getCategories(), "Psychology", "Science")
        );
    }

    @Test
    void create_trims_normalize_Data_ISBN13(){
        Book book = new Book(
                VALID_ISBN13,
                addSpacesForString(TITLE),
                PUBLICATION_YEAR,
                new Author(addSpacesForString(AUTHOR_FIRST_NAME), addSpacesForString(AUTHOR_LAST_NAME)),
                Set.of(
                        new Category(addSpacesForString("Psychology")),
                        new Category(addSpacesForString("Science"))
                ));

        assertEquals(NORMALIZED_VALID_ISBN13, book.getIsbn());
        assertEquals(TITLE, book.getTitle());
        assertAuthor(book.getAuthor(), AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME);
        assertCategoryNames(book.getCategories(), "Psychology", "Science");
    }

    @Test
    void create_trims_normalize_Data_ISBN10(){
        Book book = new Book(
                VALID_ISBN10,
                addSpacesForString(TITLE),
                PUBLICATION_YEAR,
                new Author(addSpacesForString(AUTHOR_FIRST_NAME), addSpacesForString(AUTHOR_LAST_NAME)),
                Set.of(
                        new Category(addSpacesForString("Psychology")),
                        new Category(addSpacesForString("Science"))
                ));

        assertEquals(NORMALIZED_VALID_ISBN10, book.getIsbn());
        assertEquals(TITLE, book.getTitle());
        assertAuthor(book.getAuthor(), AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME);
        assertCategoryNames(book.getCategories(), "Psychology", "Science");
    }

    @Test
    void create_whenISBNisNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(null, TITLE, PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
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
                () -> new Book(NORMALIZED_VALID_ISBN13, TITLE, 1300, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, TITLE, 2300, sampleAuthor(), sampleCategories()));
    }

    @Test
    void create_whenAuthorIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, TITLE, PUBLICATION_YEAR, null, sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, TITLE, PUBLICATION_YEAR, new Author("", ""), sampleCategories()));
    }

    @Test
    void create_whenCategoryIsNullOrEmptyOrContainsNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, TITLE, PUBLICATION_YEAR, sampleAuthor(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, TITLE, PUBLICATION_YEAR, sampleAuthor(), Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(NORMALIZED_VALID_ISBN13, TITLE, PUBLICATION_YEAR, sampleAuthor(), setWithNullCategory()));
    }

    @Test
    void create_whenInvalidISBN_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book("1529351125", TITLE, PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> new Book("3213213213213", TITLE, PUBLICATION_YEAR, sampleAuthor(), sampleCategories()));
    }

    @Test
    void markBorrowed_ChangesAvailabilityToFalse(){
        Book book = sampleBook();
        book.markBorrowed();
        assertFalse(book.isAvailable());
    }

    @Test
    void markBorrowed_whenBookIsNotAvailable_throwsIllegalStateException(){
        Book book = sampleBook();
        book.markBorrowed();
        assertThrows(IllegalStateException.class,
                book::markBorrowed);
    }

    @Test
    void markReturned_ChangesAvailabilityToTrue(){
        Book book = sampleBook();
        book.markBorrowed();
        book.markReturned();
        assertTrue(book.isAvailable());
    }

    @Test
    void markReturned_whenBookIsAvailable_throwsIllegalStateException(){
        Book book = sampleBook();
        assertThrows(IllegalStateException.class,
                book::markReturned);
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
                () -> book.updateDetails(TITLE, 1300, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(TITLE, 2060, sampleAuthor(), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(TITLE, PUBLICATION_YEAR, null, sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(TITLE, PUBLICATION_YEAR, new Author("", ""), sampleCategories()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(TITLE, PUBLICATION_YEAR, sampleAuthor(), Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(TITLE, PUBLICATION_YEAR, sampleAuthor(), null));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(TITLE, PUBLICATION_YEAR, sampleAuthor(), setWithNullCategory()));
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

    private Book sampleBook(){
        return new Book(
                VALID_ISBN13,
                TITLE,
                PUBLICATION_YEAR,
                sampleAuthor(),
                sampleCategories());
    }

    private Author sampleAuthor(){
        return new Author(AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME);
    }

    private Set<Category> sampleCategories(){
        return Set.of(new Category("Psychology"), new Category("Science"));
    }

    private Set<Category> setWithNullCategory(){
        Set<Category> categories = new java.util.HashSet<>();
        categories.add(new Category("Psychology"));
        categories.add(null);
        return categories;
    }

    private String addSpacesForString(String string){
        return "     " + string + "        ";
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

package com.wikt0r2115.library.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {
    private String validISBN13 = "978-1-60309-502-0";
    private String validISBN10 = "152935112x";
    private String normalizedValidISBN13 = "9781603095020";
    private String normalizedValidISBN10 = "152935112X";
    private String title = "Atomic Habits";
    private int publicationYear = 2005;
    private String author = "James Clear";
    private String category = "Psychology";

    @Test
    void create_validBook(){
        Book book = sampleBook();
        assertAll(
                () -> assertNull(book.getBookId()),
                () -> assertEquals(normalizedValidISBN13,book.getIsbn()),
                () -> assertEquals(title, book.getTitle()),
                () -> assertEquals(publicationYear, book.getPublicationYear()),
                () -> assertTrue(book.isAvailable()),
                () -> assertEquals(author, book.getAuthor()),
                () -> assertEquals(category, book.getCategory())
        );
    }
    @Test
    void create_trims_normalize_Data_ISBN13(){
        Book book = new Book(validISBN13,
                "   Atomic Habits    ",
                publicationYear,
                "   James Clear  ",
                "  Psychology");
        assertEquals(normalizedValidISBN13, book.getIsbn());
        assertEquals(title, book.getTitle());
        assertEquals(author, book.getAuthor());
        assertEquals(category, book.getCategory());
    }

    @Test
    void create_trims_normalize_Data_ISBN10(){
        Book book = new Book(validISBN10,
                "   Atomic Habits    ",
                publicationYear,
                "   James Clear  ",
                "  Psychology");
        assertEquals(normalizedValidISBN10, book.getIsbn());
        assertEquals(title, book.getTitle());
        assertEquals(author, book.getAuthor());
        assertEquals(category, book.getCategory());
    }

    @Test
    void create_whenISBNisNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(null,title,publicationYear,author,category));
    }

    @Test
    void create_whenTitleIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(normalizedValidISBN13,null,publicationYear,author,category));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(normalizedValidISBN13, "", publicationYear,author,category));
    }

    @Test
    void create_whenPublicationYearIsLowerThan1450ORHigherThanCurrentYear_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(normalizedValidISBN13,title,1300,author,category));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(normalizedValidISBN13,title, 2300,author,category));
    }

    @Test
    void create_whenAuthorIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(normalizedValidISBN13,title,publicationYear,null,category));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(normalizedValidISBN13,title,publicationYear,"",category));
    }

    @Test
    void create_whenCategoryIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Book(normalizedValidISBN13,title,publicationYear,author,null));
        assertThrows(IllegalArgumentException.class,
                () -> new Book(normalizedValidISBN13,title,publicationYear,author,""));
    }

    @Test
    void create_whenInvalidISBN_throwsIllegalArgumentException(){
        //ISBN10
        assertThrows(IllegalArgumentException.class,
                () -> new Book("1529351125",title,publicationYear,author,category));
        //ISBN13
        assertThrows(IllegalArgumentException.class,
                () -> new Book("3213213213213",title,publicationYear,author,category));
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
                () -> book.markBorrowed());
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
                () -> book.markReturned());
    }

    @Test
    void updateDetails_whenValidData_changesData(){
        Book book = sampleBook();
        String oldIsbn = book.getIsbn();
        boolean oldAvailability = book.isAvailable();
        book.updateDetails("Title",2018,"Author","Category");
        assertEquals("Title",book.getTitle());
        assertEquals(2018,book.getPublicationYear());
        assertEquals("Author",book.getAuthor());
        assertEquals("Category",book.getCategory());
        assertEquals(oldIsbn, book.getIsbn());
        assertEquals(oldAvailability, book.isAvailable());
    }

    @Test
    void updateDetails_whenInvalidData_throwsIllegalArgumentException(){
        Book book = sampleBook();
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails("",publicationYear,author,category));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(null,publicationYear,author,category));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(title,1300,author,category));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(title,2060,author,category));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(title,publicationYear,"",category));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(title,publicationYear,null,category));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(title,publicationYear,author,""));
        assertThrows(IllegalArgumentException.class,
                () -> book.updateDetails(title,publicationYear,author,null));
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
        book.changeIsbn(validISBN10);
        assertEquals(normalizedValidISBN10,book.getIsbn());
    }

    @Test
    void changeIsbn_whenxinIsbn10_normalizeIsbn(){
        Book book = sampleBook();
        book.changeIsbn("152935112x");
        assertEquals(normalizedValidISBN10,book.getIsbn());
    }

    @Test
    void changeIsbn_whenInvalidIsbn13_ThrowsIllegalArgumentException(){
        Book book = sampleBook();
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn(""));
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn(null));
        //prefix
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("974-1-60309-502-0"));
        //checksum
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("978-1-60309-502-1"));
        //character
        assertThrows(IllegalArgumentException.class,
                () -> book.changeIsbn("978-1-60309-502-X"));
        //length
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
        return new Book(validISBN13,
                title,
                publicationYear,
                author,
                category);
    }
}

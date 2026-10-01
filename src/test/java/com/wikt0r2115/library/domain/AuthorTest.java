package com.wikt0r2115.library.domain;

import org.junit.jupiter.api.Test;

import static com.wikt0r2115.library.TestData.AUTHOR_FIRST_NAME;
import static com.wikt0r2115.library.TestData.AUTHOR_LAST_NAME;
import static org.junit.jupiter.api.Assertions.*;

class AuthorTest {
    @Test
    void create_validAuthor() {
        Author author = new Author(AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME);
        assertEquals(AUTHOR_FIRST_NAME, author.getFirstName());
        assertEquals(AUTHOR_LAST_NAME, author.getLastName());
    }

    @Test
    void create_trimsValues() {
        Author author = new Author("   "+AUTHOR_FIRST_NAME+"    ","    "+AUTHOR_LAST_NAME+"   ");
        assertEquals(AUTHOR_FIRST_NAME, author.getFirstName());
        assertEquals(AUTHOR_LAST_NAME, author.getLastName());
    }

    @Test
    void create_whenFirstNameIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class , () -> new Author(null, AUTHOR_LAST_NAME));
    }

    @Test
    void create_whenLastNameIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,() -> new Author(AUTHOR_FIRST_NAME, null));
    }

    @Test
    void create_whenFirstNameIsBlank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Author("", AUTHOR_LAST_NAME));
    }

    @Test
    void create_whenLastNameIsBlank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Author(AUTHOR_FIRST_NAME, ""));
    }

    @Test
    void create_whenNameExceeds255Characters_throwsIllegalArgumentException() {
        String tooLong = "x".repeat(256);
        assertThrows(IllegalArgumentException.class, () -> new Author(tooLong, AUTHOR_LAST_NAME));
        assertThrows(IllegalArgumentException.class, () -> new Author(AUTHOR_FIRST_NAME, tooLong));
    }
}

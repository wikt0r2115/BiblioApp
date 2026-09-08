package com.wikt0r2115.library.service;

import com.wikt0r2115.library.domain.Author;
import com.wikt0r2115.library.infrastructure.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {
    @Mock
    private AuthorRepository authorRepository;
    private AuthorService authorService;
    private static final String FIRST_NAME = "Jan";
    private static final String LAST_NAME = "Kowalski";

    @BeforeEach
    void setUp(){
        authorService = new AuthorService(authorRepository);
    }

    @Test
    void createAuthor_savesAuthor(){
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Author author = authorService.createAuthor(FIRST_NAME,LAST_NAME);

        assertEquals(FIRST_NAME, author.getFirstName());
        assertEquals(LAST_NAME, author.getLastName());

        verify(authorRepository).save(author);
    }

    @Test
    void createAuthor_trimsValues(){
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Author author = authorService.createAuthor("   "+FIRST_NAME+"   ","   "+LAST_NAME+"    ");

        assertEquals(FIRST_NAME, author.getFirstName());
        assertEquals(LAST_NAME, author.getLastName());

        verify(authorRepository).save(author);
    }

    @Test
    void createAuthor_withBlankFirstName_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class, () -> authorService.createAuthor("",LAST_NAME));

        verify(authorRepository, never()).save(any());
    }

    @Test
    void createAuthor_withBlankLastName_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class, () -> authorService.createAuthor(FIRST_NAME, ""));

        verify(authorRepository, never()).save(any());
    }

    @Test
    void createAuthor_withNullFirstName_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class, () -> authorService.createAuthor(null, LAST_NAME));

        verify(authorRepository, never()).save(any());
    }

    @Test
    void createAuthor_withNullLastName_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class, () -> authorService.createAuthor(FIRST_NAME, null));

        verify(authorRepository, never()).save(any());
    }

}

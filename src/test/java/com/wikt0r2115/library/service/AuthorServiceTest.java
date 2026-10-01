package com.wikt0r2115.library.service;

import com.wikt0r2115.library.controller.dto.AuthorResponse;
import com.wikt0r2115.library.domain.Author;
import com.wikt0r2115.library.infrastructure.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.wikt0r2115.library.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {
    @Mock
    private AuthorRepository authorRepository;

    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        authorService = new AuthorService(authorRepository);
    }

    @Test
    void createAuthor_whenNamesAreValid_savesNormalizedAuthor() {
        when(authorRepository.save(any(Author.class))).thenReturn(sampleAuthorWithId());

        AuthorResponse response = authorService.createAuthor(
                padded(AUTHOR_FIRST_NAME), padded(AUTHOR_LAST_NAME));

        assertEquals(new AuthorResponse(AUTHOR_ID, AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME), response);
        verify(authorRepository).save(argThat(author ->
                AUTHOR_FIRST_NAME.equals(author.getFirstName())
                        && AUTHOR_LAST_NAME.equals(author.getLastName())));
    }

    @Test
    void createAuthor_whenNameIsInvalid_doesNotSave() {
        assertThrows(IllegalArgumentException.class, () -> authorService.createAuthor(null, AUTHOR_LAST_NAME));
        assertThrows(IllegalArgumentException.class, () -> authorService.createAuthor("  ", AUTHOR_LAST_NAME));
        assertThrows(IllegalArgumentException.class, () -> authorService.createAuthor(AUTHOR_FIRST_NAME, null));
        assertThrows(IllegalArgumentException.class, () -> authorService.createAuthor(AUTHOR_FIRST_NAME, "  "));
        verifyNoInteractions(authorRepository);
    }

    @Test
    void findAll_whenAuthorsExist_returnsMappedResponses() {
        when(authorRepository.findAll()).thenReturn(List.of(sampleAuthorWithId(), sampleNewAuthor()));

        assertEquals(List.of(
                new AuthorResponse(AUTHOR_ID, AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME),
                new AuthorResponse(null, NEW_AUTHOR_FIRST_NAME, NEW_AUTHOR_LAST_NAME)),
                authorService.findAll());
    }

    @Test
    void findAll_whenNoAuthorsExist_returnsEmptyList() {
        when(authorRepository.findAll()).thenReturn(List.of());

        assertEquals(List.of(), authorService.findAll());
    }
}

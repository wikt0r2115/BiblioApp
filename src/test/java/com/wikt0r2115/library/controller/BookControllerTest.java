package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.domain.Author;
import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Category;
import com.wikt0r2115.library.service.BookNotFoundException;
import com.wikt0r2115.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {
    @Mock
    private BookService bookService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new BookController(bookService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .build();
    }

    @Test
    void findBookById_whenBookExists_returnsBook() throws Exception {
        when(bookService.findById(1L))
                .thenReturn(sampleBook());

        mockMvc.perform(get("/book/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isbn").value("9781603095020"))
                .andExpect(jsonPath("$.title").value("Atomic Habits"))
                .andExpect(jsonPath("$.publicationYear").value(2024))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.author.id").value(2))
                .andExpect(jsonPath("$.author.firstName").value("James"))
                .andExpect(jsonPath("$.author.lastName").value("Clear"))
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.categories[0].categoryId").value(3))
                .andExpect(jsonPath("$.categories[0].name").value("Psychology"));
    }

    @Test
    void findBookById_whenBookDoesntExist_returnsNotFound() throws Exception {
        when(bookService.findById(99L))
                .thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/book/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.detail").value("Book does not exist"));
    }

    @Test
    void createBook_whenRequestIsValid_returnsCreatedBook() throws Exception {
        when(bookService.createBook(
                eq("9781603095020"),
                eq("Atomic Habits"),
                eq(2024),
                eq(2L),
                eq(Set.of(3L))))
                .thenReturn(sampleBook());

        mockMvc.perform(post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "9781603095020",
                                  "title": "Atomic Habits",
                                  "publicationYear": 2024,
                                  "authorId": 2,
                                  "categoryIds": [3]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.author.id").value(2))
                .andExpect(jsonPath("$.categories[0].categoryId").value(3));
    }

    @Test
    void createBook_whenRequestIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "",
                                  "title": "",
                                  "publicationYear": 1200,
                                  "categoryIds": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid"));
    }

    @Test
    void findBooks_withFilters_returnsPage() throws Exception {
        when(bookService.findAll(eq(2L), eq("Atomic"), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBook()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/book")
                        .param("authorId", "2")
                        .param("title", "Atomic")
                        .param("available", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].author.id").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void updateDetails_whenRequestIsValid_returnsUpdatedBook() throws Exception {
        when(bookService.updateDetails(
                eq(1L),
                eq("Clean Code"),
                eq(2025),
                eq(2L),
                eq(Set.of(3L))))
                .thenReturn(sampleBook("Clean Code", 2025));

        mockMvc.perform(put("/book/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Clean Code",
                                  "publicationYear": 2025,
                                  "authorId": 2,
                                  "categoryIds": [3]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.publicationYear").value(2025));
    }

    @Test
    void changeIsbn_whenRequestIsValid_returnsUpdatedBook() throws Exception {
        Book book = sampleBook();
        book.changeIsbn("9780136091813");
        when(bookService.changeIsbn(1L, "9780136091813"))
                .thenReturn(book);

        mockMvc.perform(put("/book/1/isbn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "9780136091813"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("9780136091813"));
    }

    @Test
    void deleteBook_whenBookExists_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/book/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(1L);
    }

    private Book sampleBook() {
        return sampleBook("Atomic Habits", 2024);
    }

    private Book sampleBook(String title, int publicationYear) {
        Author author = new Author("James", "Clear");
        ReflectionTestUtils.setField(author, "authorId", 2L);

        Category category = new Category("Psychology");
        ReflectionTestUtils.setField(category, "categoryId", 3L);

        Book book = new Book("9781603095020", title, publicationYear, author, Set.of(category));
        ReflectionTestUtils.setField(book, "bookId", 1L);
        return book;
    }
}

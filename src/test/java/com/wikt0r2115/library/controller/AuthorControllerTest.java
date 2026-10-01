package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.controller.dto.AuthorResponse;
import com.wikt0r2115.library.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {
    @Mock
    private AuthorService authorService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthorController(authorService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void findAuthors_returnsAuthors() throws Exception {
        when(authorService.findAll())
                .thenReturn(List.of(new AuthorResponse(1L, "James", "Clear")));

        mockMvc.perform(get("/author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("James"))
                .andExpect(jsonPath("$[0].lastName").value("Clear"));
    }

    @Test
    void createAuthor_whenRequestIsValid_returnsCreatedAuthor() throws Exception {
        when(authorService.createAuthor("James", "Clear"))
                .thenReturn(new AuthorResponse(1L, "James", "Clear"));

        mockMvc.perform(post("/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "James",
                                  "lastName": "Clear"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("James"))
                .andExpect(jsonPath("$.lastName").value("Clear"));

        verify(authorService).createAuthor("James", "Clear");
    }

    @Test
    void createAuthor_whenRequestIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));

        verifyNoInteractions(authorService);
    }

    @Test
    void findAuthors_whenNoAuthorsExist_returnsEmptyList() throws Exception {
        when(authorService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createAuthor_whenServiceRejectsData_returnsBadRequest() throws Exception {
        when(authorService.createAuthor("James", "Clear"))
                .thenThrow(new IllegalArgumentException("Invalid author"));

        mockMvc.perform(post("/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"James\",\"lastName\":\"Clear\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request data"))
                .andExpect(jsonPath("$.detail").value("Invalid author"));
    }

    @Test
    void createAuthor_whenDataConflicts_returnsConflict() throws Exception {
        when(authorService.createAuthor("James", "Clear"))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        mockMvc.perform(post("/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"James\",\"lastName\":\"Clear\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Data integrity conflict"))
                .andExpect(jsonPath("$.detail").value("Request conflicts with existing data"));
    }

    @Test
    void createAuthor_whenUnexpectedErrorOccurs_returnsServerError() throws Exception {
        when(authorService.createAuthor("James", "Clear"))
                .thenThrow(new RuntimeException("internal details"));

        mockMvc.perform(post("/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"James\",\"lastName\":\"Clear\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Unexpected error"))
                .andExpect(jsonPath("$.detail").value("Unexpected error occurred"));
    }

}

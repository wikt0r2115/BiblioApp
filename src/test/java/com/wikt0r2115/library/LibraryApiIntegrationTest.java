package com.wikt0r2115.library;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@Transactional
class LibraryApiIntegrationTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).build();
    }

    @Test
    void loanFlow_borrowReturnAndArchivePreservesHistory() throws Exception {
        long authorId = createAuthor("James", "Clear");
        long categoryId = createCategory("Psychology");
        long memberId = createMember("Jan", "Kowalski", "jan@example.com");
        long bookId = createBook("9781603095020", "Atomic Habits", authorId, categoryId);

        mockMvc.perform(put("/member/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Kuba","lastName":"Kowal","email":"kuba@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("kuba@example.com"));

        String borrowRequest = """
                {"bookId":%d,"memberId":%d}
                """.formatted(bookId, memberId);
        MvcResult borrowed = mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(borrowRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.memberFirstName").value("Kuba"))
                .andReturn();
        long loanId = idFrom(borrowed, "$.loanId");

        mockMvc.perform(get("/book/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
        mockMvc.perform(get("/members/{id}/loans/active", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].loanId").value(loanId));
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(borrowRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Book is loaned"));
        mockMvc.perform(delete("/book/{id}", bookId))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/member/{id}", memberId))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/loans/{id}/return", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.returnedAt").exists());
        mockMvc.perform(get("/book/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
        mockMvc.perform(get("/members/{id}/loans/active", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(delete("/book/{id}", bookId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/member/{id}", memberId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/book/{id}", bookId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/member/{id}", memberId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/members/{id}/loans/history", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].loanId").value(loanId))
                .andExpect(jsonPath("$[0].active").value(false));
    }

    @Test
    void catalogFlow_updateBookAndSearchUsingCurrentDetails() throws Exception {
        long firstAuthorId = createAuthor("James", "Clear");
        long secondAuthorId = createAuthor("Robert", "Martin");
        long firstCategoryId = createCategory("Psychology");
        long secondCategoryId = createCategory("Programming");
        long bookId = createBook("9781603095020", "Atomic Habits", firstAuthorId, firstCategoryId);

        mockMvc.perform(put("/book/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","publicationYear":2025,
                                 "authorId":%d,"categoryIds":[%d]}
                                """.formatted(secondAuthorId, secondCategoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author.id").value(secondAuthorId))
                .andExpect(jsonPath("$.categories[0].categoryId").value(secondCategoryId));
        mockMvc.perform(put("/book/{id}/isbn", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"9780136091813\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("9780136091813"));

        mockMvc.perform(get("/book")
                        .param("authorId", Long.toString(secondAuthorId))
                        .param("title", "clean")
                        .param("available", "true")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(bookId))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/book")
                        .param("authorId", Long.toString(firstAuthorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
        mockMvc.perform(get("/book/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("9780136091813"));
    }

    private long createAuthor(String firstName, String lastName) throws Exception {
        MvcResult result = mockMvc.perform(post("/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"%s","lastName":"%s"}
                                """.formatted(firstName, lastName)))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result, "$.id");
    }

    private long createCategory(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result, "$.categoryId");
    }

    private long createMember(String firstName, String lastName, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"%s","lastName":"%s","email":"%s"}
                                """.formatted(firstName, lastName, email)))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result, "$.memberId");
    }

    private long createBook(String isbn, String title, long authorId, long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"%s","title":"%s","publicationYear":2024,
                                 "authorId":%d,"categoryIds":[%d]}
                                """.formatted(isbn, title, authorId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result, "$.id");
    }

    private long idFrom(MvcResult result, String path) throws Exception {
        Number value = JsonPath.read(result.getResponse().getContentAsString(), path);
        return value.longValue();
    }
}

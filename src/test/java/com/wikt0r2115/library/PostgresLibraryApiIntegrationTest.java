package com.wikt0r2115.library;

import com.wikt0r2115.library.controller.dto.LoanResponse;
import com.wikt0r2115.library.service.BookService;
import com.wikt0r2115.library.service.LoanService;
import com.wikt0r2115.library.service.MemberService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ActiveProfiles("postgres")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Sql(statements = "TRUNCATE loan, book_category, book, member, category, author CASCADE")
class PostgresLibraryApiIntegrationTest extends LibraryApiIntegrationTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired LoanService loans;
    @Autowired BookService books;
    @Autowired MemberService members;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;

    @Test
    void concurrentBorrowAndReturnKeepOneActiveLoanAndOneReturn() throws Exception {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        long author = createAuthor("Anna", "Author");
        long category = createCategory("Concurrency");
        long book = createBook("9781603095020", "Race", author, category);
        long first = createMember("First", "Reader", "first@example.com");
        long second = createMember("Second", "Reader", "second@example.com");

        List<Object> borrowed = race(() -> loans.borrowBook(book, first),
                () -> loans.borrowBook(book, second));
        assertEquals(1, borrowed.stream().filter(LoanResponse.class::isInstance).count());
        assertEquals(1, borrowed.stream().filter(RuntimeException.class::isInstance).count());
        assertEquals(1, count("SELECT count(*) FROM loan WHERE book_book_id = ? AND returned_at IS NULL", book));
        assertFalse(available(book));

        long loan = borrowed.stream().filter(LoanResponse.class::isInstance)
                .map(LoanResponse.class::cast).findFirst().orElseThrow().loanId();
        List<Object> returned = race(() -> loans.returnBook(loan), () -> loans.returnBook(loan));
        assertEquals(1, returned.stream().filter(LoanResponse.class::isInstance).count());
        assertEquals(1, returned.stream().filter(IllegalStateException.class::isInstance).count());
        assertEquals(0, count("SELECT count(*) FROM loan WHERE book_book_id = ? AND returned_at IS NULL", book));
        assertEquals(1, count("SELECT count(*) FROM loan WHERE book_book_id = ?", book));
        assertTrue(available(book));
    }

    @Test
    void archiveRacesWithBorrowWithoutLosingHistory() throws Exception {
        long author = createAuthor("Anna", "Author");
        long category = createCategory("Concurrency");
        long book = createBook("9781603095020", "Archive race", author, category);
        long member = createMember("First", "Reader", "first@example.com");

        List<Object> bookRace = race(() -> loans.borrowBook(book, member), () -> {
            books.deleteBook(book);
            return "archived";
        });
        assertEquals(1, bookRace.stream().filter(RuntimeException.class::isInstance).count());
        assertEquals(1, count("SELECT count(*) FROM loan WHERE book_book_id = ? AND returned_at IS NULL", book)
                + count("SELECT count(*) FROM book WHERE book_id = ? AND archived", book));

        if (bookRace.stream().anyMatch(LoanResponse.class::isInstance)) {
            long loan = bookRace.stream().filter(LoanResponse.class::isInstance)
                    .map(LoanResponse.class::cast).findFirst().orElseThrow().loanId();
            loans.returnBook(loan);
            assertEquals(1, count("SELECT count(*) FROM loan WHERE book_book_id = ?", book));
        }

        long otherBook = createBook("9780136091813", "Member race", author, category);
        long otherMember = createMember("Second", "Reader", "second@example.com");
        List<Object> memberRace = race(() -> loans.borrowBook(otherBook, otherMember), () -> {
            members.deleteMember(otherMember);
            return "archived";
        });
        assertEquals(1, memberRace.stream().filter(RuntimeException.class::isInstance).count());
        assertEquals(1, count("SELECT count(*) FROM loan WHERE member_member_id = ? AND returned_at IS NULL", otherMember)
                + count("SELECT count(*) FROM member WHERE member_id = ? AND archived", otherMember));
    }

    @Test
    void migrationsAreRepeatableAndPaginationUsesPostgres() throws Exception {
        long author = createAuthor("Anna", "Author");
        long category = createCategory("Search");
        createBook("9781603095020", "First", author, category);
        createBook("9780136091813", "Second", author, category);

        mockMvc.perform(get("/book").param("size", "1").param("page", "1")
                        .param("available", "true").param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Second"))
                .andExpect(jsonPath("$.totalElements").value(2));

        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertEquals(2, count("SELECT count(*) FROM book"));
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    private boolean available(long bookId) {
        return jdbc.queryForObject("SELECT available FROM book WHERE book_id = ?", Boolean.class, bookId);
    }

    private List<Object> race(Callable<?> first, Callable<?> second) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Future<Object> a = pool.submit(() -> attempt(ready, start, first));
            Future<Object> b = pool.submit(() -> attempt(ready, start, second));
            boolean bothReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            assertTrue(bothReady);
            return List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        }
    }

    private Object attempt(CountDownLatch ready, CountDownLatch start, Callable<?> work) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            return work.call();
        } catch (Exception failure) {
            return failure;
        }
    }
}

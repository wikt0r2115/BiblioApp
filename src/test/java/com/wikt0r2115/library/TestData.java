package com.wikt0r2115.library;

import com.wikt0r2115.library.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TestData {
    public static final String VALID_ISBN13 = "978-1-60309-502-0";
    public static final String VALID_ISBN10 = "152935112x";
    public static final String NORMALIZED_VALID_ISBN13 = "9781603095020";
    public static final String NORMALIZED_VALID_ISBN10 = "152935112X";
    public static final String BOOK_TITLE = "Atomic Habits";
    public static final int PUBLICATION_YEAR = 2005;
    public static final String NEW_ISBN = "9780136091813";
    public static final String NEW_BOOK_TITLE = "TITLE";
    public static final int NEW_PUBLICATION_YEAR = 2018;
    public static final String AUTHOR_FIRST_NAME = "James";
    public static final String AUTHOR_LAST_NAME = "Clear";
    public static final String NEW_AUTHOR_FIRST_NAME = "Robert";
    public static final String NEW_AUTHOR_LAST_NAME = "Martin";
    public static final String CATEGORY_ONE = "Psychology";
    public static final String CATEGORY_TWO = "Science";
    public static final String NEW_CATEGORY_ONE = "Programming";
    public static final String NEW_CATEGORY_TWO = "Craft";
    public static final String MEMBER_FIRST_NAME = "Jan";
    public static final String MEMBER_LAST_NAME = "Kowalski";
    public static final String MEMBER_EMAIL = "jan@example.com";
    public static final String NEW_MEMBER_FIRST_NAME = "Kuba";
    public static final String NEW_MEMBER_LAST_NAME = "Kowal";
    public static final String NEW_MEMBER_EMAIL = "kuba@example.com";
    public static final List<String> INVALID_EMAILS = List.of("test", "test@", "test@test");
    public static final Long BOOK_ID = 1L;
    public static final Long AUTHOR_ID = 20L;
    public static final Long NEW_AUTHOR_ID = 21L;
    public static final Long CATEGORY_ID = 30L;
    public static final Long SECOND_CATEGORY_ID = 31L;
    public static final Long NEW_CATEGORY_ID = 32L;
    public static final Long SECOND_NEW_CATEGORY_ID = 33L;
    public static final Set<Long> CATEGORY_IDS = Set.of(CATEGORY_ID, SECOND_CATEGORY_ID);
    public static final Set<Long> NEW_CATEGORY_IDS = Set.of(NEW_CATEGORY_ID, SECOND_NEW_CATEGORY_ID);
    public static final Long MEMBER_ID = 2L;
    public static final Long LOAN_ID = 10L;

    private TestData() {
    }

    public static Author sampleAuthor() {
        return new Author(AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME);
    }

    public static Author sampleAuthorWithId() {
        Author author = sampleAuthor();
        ReflectionTestUtils.setField(author, "authorId", AUTHOR_ID);
        return author;
    }

    public static Author sampleNewAuthor() {
        return new Author(NEW_AUTHOR_FIRST_NAME, NEW_AUTHOR_LAST_NAME);
    }

    public static Category sampleCategory() {
        return new Category(CATEGORY_ONE);
    }

    public static Category sampleCategoryWithId() {
        Category category = sampleCategory();
        ReflectionTestUtils.setField(category, "categoryId", CATEGORY_ID);
        return category;
    }

    public static Set<Category> sampleCategories() {
        return Set.of(new Category(CATEGORY_ONE), new Category(CATEGORY_TWO));
    }

    public static Set<Category> sampleNewCategories() {
        return Set.of(new Category(NEW_CATEGORY_ONE), new Category(NEW_CATEGORY_TWO));
    }

    public static Set<Category> categoriesWithNull() {
        Set<Category> categories = new HashSet<>();
        categories.add(new Category(CATEGORY_ONE));
        categories.add(null);
        return categories;
    }

    public static Book sampleBook() {
        return new Book(
                VALID_ISBN13,
                BOOK_TITLE,
                PUBLICATION_YEAR,
                sampleAuthor(),
                sampleCategories()
        );
    }

    public static Book sampleBookWithIsbn10() {
        return new Book(
                NORMALIZED_VALID_ISBN10,
                BOOK_TITLE,
                PUBLICATION_YEAR,
                sampleAuthor(),
                sampleCategories()
        );
    }

    public static Book sampleBookWithId() {
        Book book = new Book(
                NORMALIZED_VALID_ISBN13,
                BOOK_TITLE,
                2024,
                sampleAuthorWithId(),
                Set.of(sampleCategoryWithId())
        );
        ReflectionTestUtils.setField(book, "bookId", BOOK_ID);
        return book;
    }

    public static Book sampleArchivedBook() {
        Book book = sampleBook();
        book.markArchived();
        return book;
    }

    public static Member sampleMember() {
        return new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, MEMBER_EMAIL);
    }

    public static Member sampleMemberWithId() {
        Member member = sampleMember();
        ReflectionTestUtils.setField(member, "memberId", MEMBER_ID);
        return member;
    }

    public static Member sampleArchivedMember() {
        Member member = sampleMemberWithId();
        member.markArchived();
        return member;
    }

    public static Loan sampleLoan() {
        return new Loan(sampleBook(), sampleMember());
    }

    public static Loan sampleActiveLoanWithIds() {
        Loan loan = new Loan(sampleBookWithId(), sampleMemberWithId());
        ReflectionTestUtils.setField(loan, "loanId", LOAN_ID);
        ReflectionTestUtils.setField(loan, "borrowedAt", LocalDateTime.of(2026, 7, 20, 12, 0));
        return loan;
    }

    public static Loan sampleReturnedLoanWithIds() {
        Loan loan = sampleActiveLoanWithIds();
        ReflectionTestUtils.setField(loan, "returnedAt", LocalDateTime.of(2026, 7, 27, 15, 30));
        return loan;
    }

    public static void setLoanBorrowedAt(Loan loan, LocalDateTime borrowedAt) {
        ReflectionTestUtils.setField(loan, "borrowedAt", borrowedAt);
    }

    public static void setLoanReturnedAt(Loan loan, LocalDateTime returnedAt) {
        ReflectionTestUtils.setField(loan, "returnedAt", returnedAt);
    }

    public static String padded(String value) {
        return "     " + value + "        ";
    }
}

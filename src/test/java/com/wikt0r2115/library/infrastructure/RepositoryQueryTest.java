package com.wikt0r2115.library.infrastructure;

import com.wikt0r2115.library.domain.Author;
import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Category;
import com.wikt0r2115.library.domain.Loan;
import com.wikt0r2115.library.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static com.wikt0r2115.library.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RepositoryQueryTest {
    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LoanRepository loanRepository;

    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        author = authorRepository.save(new Author(AUTHOR_FIRST_NAME, AUTHOR_LAST_NAME));
        category = categoryRepository.save(new Category(CATEGORY_ONE));
    }

    @Test
    void activeLookups_excludeArchivedBooksAndMembers() {
        Book book = saveBook(VALID_ISBN13, BOOK_TITLE);
        Member member = memberRepository.save(new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, MEMBER_EMAIL));

        assertTrue(bookRepository.findByIdWhereArchivedFalse(book.getBookId()).isPresent());
        assertTrue(memberRepository.findByIdWhereArchivedFalse(member.getMemberId()).isPresent());

        book.markArchived();
        member.markArchived();
        bookRepository.flush();
        memberRepository.flush();

        assertTrue(bookRepository.findById(book.getBookId()).isPresent());
        assertTrue(memberRepository.findById(member.getMemberId()).isPresent());
        assertFalse(bookRepository.findByIdWhereArchivedFalse(book.getBookId()).isPresent());
        assertFalse(memberRepository.findByIdWhereArchivedFalse(member.getMemberId()).isPresent());
    }

    @Test
    void findWithFilters_matchesTitleIgnoringCaseAndExcludesArchivedBooks() {
        Book matching = saveBook(VALID_ISBN13, BOOK_TITLE);
        Book archived = saveBook(NEW_ISBN, "Atomic Reference");
        archived.markArchived();
        bookRepository.flush();

        Page<Book> page = bookRepository.findWithFilters(
                matching.getAuthor().getAuthorId(), "ATOMIC", true,
                PageRequest.of(0, 10, Sort.by("title")));

        assertEquals(1, page.getTotalElements());
        assertEquals(matching.getBookId(), page.getContent().getFirst().getBookId());
    }

    @Test
    void loanQueries_separateActiveAndReturnedLoans() {
        Book activeBook = saveBook(VALID_ISBN13, BOOK_TITLE);
        Book returnedBook = saveBook(NEW_ISBN, "Clean Code");
        Member member = memberRepository.save(new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, MEMBER_EMAIL));
        Loan active = loanRepository.save(new Loan(activeBook, member));
        Loan returned = new Loan(returnedBook, member);
        setLoanBorrowedAt(returned, LocalDateTime.of(2020, 1, 1, 12, 0));
        returned.returnBook();
        returned = loanRepository.save(returned);
        loanRepository.flush();

        assertEquals(active.getLoanId(),
                loanRepository.findByBookAndReturnedAtIsNull(activeBook).orElseThrow().getLoanId());
        assertEquals(active.getLoanId(),
                loanRepository.findByMemberAndReturnedAtIsNull(member).getFirst().getLoanId());
        assertEquals(returned.getLoanId(),
                loanRepository.findByMemberAndReturnedAtIsNotNull(member).getFirst().getLoanId());
        assertTrue(loanRepository.findByBookAndReturnedAtIsNull(returnedBook).isEmpty());
    }

    private Book saveBook(String isbn, String title) {
        return bookRepository.save(new Book(isbn, title, PUBLICATION_YEAR, author, Set.of(category)));
    }
}

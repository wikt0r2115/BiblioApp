package com.wikt0r2115.library.service;

import com.wikt0r2115.library.controller.dto.LoanResponse;
import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Loan;
import com.wikt0r2115.library.domain.Member;
import com.wikt0r2115.library.infrastructure.BookRepository;
import com.wikt0r2115.library.infrastructure.LoanRepository;
import com.wikt0r2115.library.infrastructure.MemberRepository;
import com.wikt0r2115.library.service.exception.BookAlreadyLoanedException;
import com.wikt0r2115.library.service.exception.BookNotFoundException;
import com.wikt0r2115.library.service.exception.LoanNotFoundException;
import com.wikt0r2115.library.service.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.wikt0r2115.library.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanRepository, bookRepository, memberRepository);
    }

    @Test
    void borrowBook_whenBookAndMemberExist_createsLoanAndMarksBookAsBorrowed() {
        Book book = sampleBook();
        Member member = sampleMember();

        when(bookRepository.findByIdWhereArchivedFalse(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findByIdWhereArchivedFalse(2L)).thenReturn(Optional.of(member));
        when(loanRepository.findByBookAndReturnedAtIsNull(book)).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse loan = loanService.borrowBook(1L, 2L);

        assertEquals(book.getTitle(), loan.bookTitle());
        assertEquals(member.getFirstName(), loan.memberFirstName());
        assertFalse(book.isAvailable());
        assertNull(loan.returnedAt());
        assertTrue(loan.active());
        verify(bookRepository).save(book);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void borrowBook_whenBookIsAlreadyUnavailable_throwsIllegalStateException() {
        Book book = sampleBook();
        book.markBorrowed();
        when(bookRepository.findByIdWhereArchivedFalse(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findByIdWhereArchivedFalse(2L)).thenReturn(Optional.of(sampleMember()));

        assertThrows(IllegalStateException.class, () -> loanService.borrowBook(1L, 2L));
    }

    @Test
    void borrowBook_whenActiveLoanAlreadyExists_throwsBookAlreadyLoanedException() {
        Book book = sampleBook();
        Member member = sampleMember();
        Loan activeLoan = new Loan(book, member);

        when(bookRepository.findByIdWhereArchivedFalse(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findByIdWhereArchivedFalse(2L)).thenReturn(Optional.of(member));
        when(loanRepository.findByBookAndReturnedAtIsNull(book)).thenReturn(Optional.of(activeLoan));


        assertThrows(BookAlreadyLoanedException.class, () -> loanService.borrowBook(1L, 2L));
    }

    @Test
    void findById_whenLoanExists_returnsLoan() {
        Loan loan = new Loan(sampleBook(), sampleMember());
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        LoanResponse result = loanService.findById(1L);

        assertEquals(LoanResponse.from(loan), result);
    }

    @Test
    void findById_whenLoanDoesNotExist_throwsLoanNotFoundException() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanService.findById(1L));
    }

    @Test
    void returnBook_whenLoanExists_marksLoanAsReturnedAndBookAsAvailable() throws Exception {
        Book book = sampleBook();
        Member member = sampleMember();
        Loan loan = new Loan(book, member);
        book.markBorrowed();
        setLoanBorrowedAt(loan, LocalDateTime.of(2020, 1, 1, 12, 0));

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse returnedLoan = loanService.returnBook(1L);

        assertTrue(book.isAvailable());
        assertNotNull(returnedLoan.returnedAt());
        assertFalse(returnedLoan.active());
        verify(bookRepository).save(book);
        verify(loanRepository).save(loan);
    }

    @Test
    void findActiveLoansByMember_whenMemberExists_returnsActiveLoans() {
        Member member = sampleMember();
        List<Loan> loans = List.of(new Loan(sampleBook(), member));

        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNull(member)).thenReturn(loans);

        List<LoanResponse> result = loanService.findActiveLoansByMember(2L);

        assertEquals(List.of(LoanResponse.from(loans.get(0))), result);
    }

    @Test
    void findReturnedLoansByMember_whenMemberExists_returnsReturnedLoans() {
        Member member = sampleMember();
        Loan loan = new Loan(sampleBook(), member);
        setLoanBorrowedAt(loan, LocalDateTime.of(2020, 1, 1, 12, 0));
        loan.returnBook();
        List<Loan> loans = List.of(loan);

        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNotNull(member)).thenReturn(loans);

        List<LoanResponse> result = loanService.findReturnedLoansByMember(2L);

        assertEquals(List.of(LoanResponse.from(loan)), result);
    }

    @Test
    void borrowBook_whenBookDoesNotExists_throwsBookNotFoundException() {
        assertThrows(BookNotFoundException.class,
                () -> loanService.borrowBook(1L, MEMBER_ID));
    }

    @Test
    void borrowBook_whenMemberDoesNotExists_throwsMemberNotFoundException() {
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.of(sampleBookWithId()));
        assertThrows(MemberNotFoundException.class,
                () -> loanService.borrowBook(BOOK_ID, 1L));
    }

    @Test
    void returnBook_whenLoanDoesNotExists_throwsLoanNotFoundException() {
        assertThrows(LoanNotFoundException.class,
                () -> loanService.returnBook(LOAN_ID));
    }

    @Test
    void returnBook_whenBookIsAlreadyReturned_throwsIllegalStateException() {
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(sampleReturnedLoanWithIds()));
        assertThrows(IllegalStateException.class,
                () -> loanService.returnBook(LOAN_ID));
    }

    @Test
    void findActiveLoansByMember_whenMemberDoesNotExists_throwsMemberNotFoundException() {
        assertThrows(MemberNotFoundException.class,
                () -> loanService.findActiveLoansByMember(MEMBER_ID));
    }

    @Test
    void findReturnedLoansByMember_whenMemberDoesNotExists_throwsMemberNotFoundException() {
        assertThrows(MemberNotFoundException.class,
                () -> loanService.findReturnedLoansByMember(MEMBER_ID));
    }

    @Test
    void borrowBook_whenBookIdIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> loanService.borrowBook(null, MEMBER_ID));
    }

    @Test
    void borrowBook_whenMemberIdIsNull_throwsIllegalArgumentException() {
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.of(sampleBook()));
        assertThrows(IllegalArgumentException.class,
                () -> loanService.borrowBook(BOOK_ID, null));
    }

    @Test
    void findById_whenLoanIdIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> loanService.findById(null));
    }

    @Test
    void returnBook_whenLoanIdIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> loanService.returnBook(null));
    }

    @Test
    void findActiveLoansByMember_whenMemberIdIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> loanService.findActiveLoansByMember(null));
    }

    @Test
    void findReturnedLoansByMember_whenMemberIdIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> loanService.findReturnedLoansByMember(null));
    }

    @Test
    void borrowBook_whenBookIsArchived_throwsBookNotFoundException() {
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID))
                .thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class,
                () -> loanService.borrowBook(BOOK_ID, MEMBER_ID));
    }

    @Test
    void borrowBook_whenMemberIsArchived_throwsMemberNotFoundException () {
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID))
                .thenReturn(Optional.of(sampleBookWithId()));
        when(memberRepository.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(Optional.empty());
        assertThrows(MemberNotFoundException.class,
                () -> loanService.borrowBook(BOOK_ID, MEMBER_ID));
    }
    @Test
    void findActiveLoansByMember_whenMemberIsArchived_returnsEmptyList() {
        Member member = sampleMemberWithId();
        member.markArchived();
        when(memberRepository.findById(MEMBER_ID))
                .thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNull(member))
                .thenReturn(List.of());
        assertEquals(List.of(), loanService.findActiveLoansByMember(MEMBER_ID));
    }
    @Test
    void findReturnedLoansByMember_whenMemberIsArchived_returnsReturnedLoans() {
        Member member = sampleMemberWithId();
        member.markArchived();
        when(memberRepository.findById(MEMBER_ID))
                .thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNotNull(member))
                .thenReturn(List.of(sampleReturnedLoanWithIds()));

        assertEquals(List.of(LoanResponse.from(sampleReturnedLoanWithIds())),
                loanService.findReturnedLoansByMember(MEMBER_ID));
    }
    @Test
    void findById_whenBookAndMemberAreArchived_returnsLoanResponse() {
        Loan loan = sampleActiveLoanWithIds();
        loan.getBook().markArchived();
        loan.getMember().markArchived();
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));

        LoanResponse response = loanService.findById(LOAN_ID);

        assertEquals(LOAN_ID, response.loanId());
        assertEquals(BOOK_ID, response.bookId());
        assertEquals(MEMBER_ID, response.memberId());
        assertTrue(response.active());
    }

    @Test
    void findActiveLoansByMember_whenNoLoansExist_returnsEmptyList() {
        Member member = sampleMemberWithId();
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNull(member)).thenReturn(List.of());

        assertEquals(List.of(), loanService.findActiveLoansByMember(MEMBER_ID));
    }

    @Test
    void findReturnedLoansByMember_whenNoLoansExist_returnsEmptyList() {
        Member member = sampleMemberWithId();
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNotNull(member)).thenReturn(List.of());

        assertEquals(List.of(), loanService.findReturnedLoansByMember(MEMBER_ID));
    }

    @Test
    void borrowBook_whenActiveLoanExists_doesNotChangeBookOrSave() {
        Book book = sampleBookWithId();
        when(bookRepository.findByIdWhereArchivedFalse(BOOK_ID)).thenReturn(Optional.of(book));
        when(memberRepository.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(Optional.of(sampleMemberWithId()));
        when(loanRepository.findByBookAndReturnedAtIsNull(book))
                .thenReturn(Optional.of(sampleActiveLoanWithIds()));

        assertThrows(BookAlreadyLoanedException.class, () -> loanService.borrowBook(BOOK_ID, MEMBER_ID));
        assertTrue(book.isAvailable());
        verify(bookRepository, never()).save(any(Book.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnBook_whenLoanIsAlreadyReturned_doesNotSave() {
        Loan loan = sampleReturnedLoanWithIds();
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> loanService.returnBook(LOAN_ID));
        verify(bookRepository, never()).save(any(Book.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }
}

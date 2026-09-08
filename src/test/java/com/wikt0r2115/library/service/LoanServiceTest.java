package com.wikt0r2115.library.service;

import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Loan;
import com.wikt0r2115.library.domain.Member;
import com.wikt0r2115.library.infrastructure.BookRepository;
import com.wikt0r2115.library.infrastructure.LoanRepository;
import com.wikt0r2115.library.infrastructure.MemberRepository;
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

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(loanRepository.findByBookAndReturnedAtIsNull(book)).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan loan = loanService.borrowBook(1L, 2L);

        assertSame(book, loan.getBook());
        assertSame(member, loan.getMember());
        assertFalse(book.isAvailable());
        assertNull(loan.getReturnedAt());
        verify(bookRepository).save(book);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void borrowBook_whenBookIsAlreadyUnavailable_throwsIllegalStateException() {
        Book book = sampleBook();
        book.markBorrowed();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(sampleMember()));

        assertThrows(IllegalStateException.class, () -> loanService.borrowBook(1L, 2L));
    }

    @Test
    void borrowBook_whenActiveLoanAlreadyExists_throwsBookAlreadyLoanedException() {
        Book book = sampleBook();
        Member member = sampleMember();
        Loan activeLoan = new Loan(book, member);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(loanRepository.findByBookAndReturnedAtIsNull(book)).thenReturn(Optional.of(activeLoan));


        assertThrows(BookAlreadyLoaned.class, () -> loanService.borrowBook(1L, 2L));
    }

    @Test
    void findById_whenLoanExists_returnsLoan() {
        Loan loan = new Loan(sampleBook(), sampleMember());
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        Loan result = loanService.findById(1L);

        assertSame(loan, result);
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
        setLoanBorrowedAt(loan, LocalDateTime.now().minusDays(1));

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan returnedLoan = loanService.returnBook(1L);

        assertTrue(book.isAvailable());
        assertNotNull(returnedLoan.getReturnedAt());
        verify(bookRepository).save(book);
        verify(loanRepository).save(loan);
    }

    @Test
    void findActiveLoansByMember_whenMemberExists_returnsActiveLoans() {
        Member member = sampleMember();
        List<Loan> loans = List.of(new Loan(sampleBook(), member));

        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNull(member)).thenReturn(loans);

        List<Loan> result = loanService.findActiveLoansByMember(2L);

        assertEquals(loans, result);
    }

    @Test
    void findReturnedLoansByMember_whenMemberExists_returnsReturnedLoans() throws Exception {
        Member member = sampleMember();
        Loan loan = new Loan(sampleBook(), member);
        setLoanBorrowedAt(loan, LocalDateTime.now().minusDays(1));
        loan.returnBook();
        List<Loan> loans = List.of(loan);

        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNotNull(member)).thenReturn(loans);

        List<Loan> result = loanService.findReturnedLoansByMember(2L);

        assertEquals(loans, result);
    }

    @Test
    void borrowBook_whenBookDoesNotExists_throwsBookNotFoundException() {
        assertThrows(BookNotFoundException.class,
                () -> loanService.borrowBook(1L, MEMBER_ID));
    }

    @Test
    void borrowBook_whenMemberDoesNotExists_throwsMemberNotFoundException(){
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(sampleBookWithId()));
        assertThrows(MemberNotFoundException.class,
                () -> loanService.borrowBook(BOOK_ID, 1L));
    }

    @Test
    void returnBook_whenLoanDoesNotExists_throwsLoanNotFoundException(){
        assertThrows(LoanNotFoundException.class,
                () -> loanService.returnBook(LOAN_ID));
    }

    @Test
    void returnBook_whenBookIsAlreadyReturned_throwsIllegalStateException(){
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(sampleReturnedLoanWithIds()));
        assertThrows(IllegalStateException.class,
                () -> loanService.returnBook(LOAN_ID));
    }

    @Test
    void findActiveLoansByMember_whenMemberDoesNotExists_throwsMemberNotFoundException(){
        assertThrows(MemberNotFoundException.class,
                () -> loanService.findActiveLoansByMember(MEMBER_ID));
    }

    @Test
    void findReturnedLoansByMember_whenMemberDoesNotExists_throwsMemberNotFoundException(){
        assertThrows(MemberNotFoundException.class,
                () -> loanService.findReturnedLoansByMember(MEMBER_ID));
    }

    @Test
    void borrowBook_whenBookIdIsNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> loanService.borrowBook(null, MEMBER_ID));
    }

    @Test
    void borrowBook_whenMemberIdIsNull_throwsIllegalArgumentException(){
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(sampleBook()));
        assertThrows(IllegalArgumentException.class,
                () -> loanService.borrowBook(BOOK_ID, null));
    }

    @Test
    void findById_whenLoanIdIsNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> loanService.findById(null));
    }

    @Test
    void returnBook_whenLoanIdIsNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> loanService.returnBook(null));
    }

    @Test
    void findActiveLoansByMember_whenMemberIdIsNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> loanService.findActiveLoansByMember(null));
    }

    @Test
    void findReturnedLoansByMember_whenMemberIdIsNull_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> loanService.findReturnedLoansByMember(null));
    }

}

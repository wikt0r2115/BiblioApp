package com.wikt0r2115.library.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.wikt0r2115.library.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

class LoanTest {
    @Test
    void create_WhenBookIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(null, sampleMember()));
    }

    @Test
    void create_whenMemberIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(sampleBook(), null));
    }

    @Test
    void returnBook_whenBorrowedAtIsInPast_marksLoanReturned() {
        Loan loan = sampleLoan();
        setLoanBorrowedAt(loan, LocalDateTime.of(2020, 1, 1, 12, 0));

        assertDoesNotThrow(loan::returnBook);
        assertNotNull(loan.getReturnedAt());
        assertFalse(loan.getReturnedAt().isBefore(loan.getBorrowedAt()));
    }

    @Test
    void returnBook_whenReturnedAtIsBeforeBorrowedAt_throwsIllegalStateException() {
        Loan loan = sampleLoan();
        setLoanBorrowedAt(loan, LocalDateTime.of(3000, 1, 1, 12, 0));

        assertThrows(IllegalStateException.class, loan::returnBook);
    }

    @Test
    void returnBook_whenLoanAlreadyReturned_throwsIllegalStateException() {
        Loan loan = sampleLoan();
        setLoanReturnedAt(loan, LocalDateTime.now());

        assertThrows(IllegalStateException.class, loan::returnBook);
    }

    @Test
    void create_validLoan_setsBorrowedAndKeepsReturnedAtNull() {
        Loan loan = sampleLoan();
        assertNotNull(loan.getBorrowedAt());
        assertNull(loan.getReturnedAt());
    }
}

package com.wikt0r2115.library.controller.dto;

import com.wikt0r2115.library.domain.Loan;

import java.time.LocalDateTime;

public record LoanResponse(
        Long loanId,
        Long bookId,
        String bookTitle,
        Long memberId,
        String memberFirstName,
        String memberLastName,
        String memberEmail,
        LocalDateTime borrowedAt,
        LocalDateTime returnedAt,
        boolean active
) {
    public static LoanResponse from(Loan loan){
        return new LoanResponse(
                loan.getLoanId(),
                loan.getBook().getBookId(),
                loan.getBook().getTitle(),
                loan.getMember().getMemberId(),
                loan.getMember().getFirstName(),
                loan.getMember().getLastName(),
                loan.getMember().getEmail(),
                loan.getBorrowedAt(),
                loan.getReturnedAt(),
                loan.getReturnedAt() == null
        );
    }
}

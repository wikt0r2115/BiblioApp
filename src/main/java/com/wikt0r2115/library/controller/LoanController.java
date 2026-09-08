package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LoanController {
    private final LoanService loanService;

    LoanController(LoanService loanService){
        this.loanService = loanService;
    }

    @PostMapping("/loans")
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse borrowBook(@Valid @RequestBody CreateLoanRequest request){
        return LoanResponse.from(loanService.borrowBook(
                request.bookId(),
                request.memberId()
        ));
    }

    @GetMapping("/loans/{loanId}")
    public LoanResponse findLoanById(@PathVariable Long loanId){
        return LoanResponse.from(loanService.findById(loanId));
    }

    @PostMapping("/loans/{loanId}/return")
    public LoanResponse returnBook(@PathVariable Long loanId){
        return LoanResponse.from(loanService.returnBook(loanId));
    }

    @GetMapping("/members/{memberId}/loans/active")
    public List<LoanResponse> getActiveLoans(@PathVariable Long memberId){
        return loanService.findActiveLoansByMember(memberId)
                .stream()
                .map(LoanResponse::from)
                .toList();
    }

    @GetMapping("/members/{memberId}/loans/history")
    public List<LoanResponse> getHistoryLoans(@PathVariable Long memberId){
        return loanService.findReturnedLoansByMember(memberId)
                .stream()
                .map(LoanResponse::from)
                .toList();
    }
}

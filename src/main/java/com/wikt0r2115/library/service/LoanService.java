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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanService(
            LoanRepository loanRepository,
            BookRepository bookRepository,
            MemberRepository memberRepository){
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public LoanResponse borrowBook(Long bookId, Long memberId){
        Book book = findBook(bookId);
        Member member = findMember(memberId);

        if (loanRepository.findByBookAndReturnedAtIsNull(book).isPresent())
            throw new BookAlreadyLoanedException(bookId);

        book.markBorrowed();
        bookRepository.save(book);
        return LoanResponse.from(loanRepository.save(new Loan(book,member)));
    }

    public LoanResponse findById(Long loanId){
        return LoanResponse.from(findLoan(loanId));
    }

    @Transactional
    public LoanResponse returnBook(Long loanId){
        Loan loan = findLoan(loanId);
        Book book = loan.getBook();

        loan.returnBook();
        book.markReturned();
        bookRepository.save(book);
        return LoanResponse.from(loanRepository.save(loan));
    }

    public List<LoanResponse> findActiveLoansByMember(Long memberId){
        Member member = findAlsoArchivedMember(memberId);
        return loanRepository.findByMemberAndReturnedAtIsNull(member).stream()
                .map(LoanResponse::from)
                .toList();
    }

    public List<LoanResponse> findReturnedLoansByMember(Long memberId){
        Member member = findAlsoArchivedMember(memberId);
        return loanRepository.findByMemberAndReturnedAtIsNotNull(member).stream()
                .map(LoanResponse::from)
                .toList();
    }


    private Book findBook(Long bookId){
        if(bookId == null)
            throw new IllegalArgumentException("Book id must not be null");
        return bookRepository.findByIdWhereArchivedFalse(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    private Member findMember(Long memberId){
        if(memberId == null)
            throw new IllegalArgumentException("Member id must not be null");
        return memberRepository.findByIdWhereArchivedFalse(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private Member findAlsoArchivedMember(Long memberId){
        if(memberId == null)
            throw new IllegalArgumentException("Member id must not be null");
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private Loan findLoan(Long loanId){
        if(loanId == null)
            throw new IllegalArgumentException("Loan id must not be null");
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
    }
}

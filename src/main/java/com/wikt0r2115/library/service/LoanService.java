package com.wikt0r2115.library.service;

import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Loan;
import com.wikt0r2115.library.domain.Member;
import com.wikt0r2115.library.infrastructure.BookRepository;
import com.wikt0r2115.library.infrastructure.LoanRepository;
import com.wikt0r2115.library.infrastructure.MemberRepository;
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
    public Loan borrowBook(Long bookId, Long memberId){
        Book book = findBook(bookId);
        Member member = findMember(memberId);

        if (loanRepository.findByBookAndReturnedAtIsNull(book).isPresent())
            throw new BookAlreadyLoaned(bookId);

        book.markBorrowed();
        bookRepository.save(book);
        return loanRepository.save(new Loan(book,member));
    }

    public Loan findById(Long loanId){
        return findLoan(loanId);
    }

    @Transactional
    public Loan returnBook(Long loanId){
        Loan loan = findLoan(loanId);
        Book book = loan.getBook();

        loan.returnBook();
        book.markReturned();
        bookRepository.save(book);
        return loanRepository.save(loan);
    }

    public List<Loan> findActiveLoansByMember(Long memberId){
        Member member = findMember(memberId);
        return loanRepository.findByMemberAndReturnedAtIsNull(member);
    }

    public List<Loan> findReturnedLoansByMember(Long memberId){
        Member member = findMember(memberId);
        return loanRepository.findByMemberAndReturnedAtIsNotNull(member);
    }


    private Book findBook(Long bookId){
        if(bookId == null)
            throw new IllegalArgumentException("Book id must not be null");
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    private Member findMember(Long memberId){
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

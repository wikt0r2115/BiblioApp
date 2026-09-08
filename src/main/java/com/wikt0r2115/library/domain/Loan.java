package com.wikt0r2115.library.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class Loan {
    @Id
    @GeneratedValue
    private Long loanId;

    @ManyToOne
    @JoinColumn(name = "book_book_id", nullable = false)
    @NotNull(message = "Book must not be null")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "member_member_id", nullable = false)
    @NotNull(message = "Member must not be null")
    private Member member;

    private LocalDateTime borrowedAt;
    private LocalDateTime returnedAt;

    protected Loan() {}

    public Loan(Book book, Member member) {
        this.book = requireBook(book);
        this.member = requireMember(member);
        this.borrowedAt = LocalDateTime.now();
    }

    public void returnBook() {
        if (returnedAt != null) {
            throw new IllegalStateException("book is already returned");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(borrowedAt)) {
            throw new IllegalStateException("return date must be after borrow date");
        }

        this.returnedAt = now;
    }

    private Book requireBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("book must not be null");
        }
        return book;
    }

    private Member requireMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member must not be null");
        }
        return member;
    }

    public Long getLoanId() { return loanId; }
    public Book getBook() { return book; }
    public Member getMember() { return member; }
    public LocalDateTime getBorrowedAt() { return borrowedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
}

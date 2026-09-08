package com.wikt0r2115.library.infrastructure;

import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Loan;
import com.wikt0r2115.library.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findByBookAndReturnedAtIsNull(Book book);
    List<Loan> findByMemberAndReturnedAtIsNull(Member member);
    List<Loan> findByMemberAndReturnedAtIsNotNull(Member member);
}

package com.wikt0r2115.library.infrastructure;

import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("""
            select m from Member m
                    where (m.memberId = :id)
                        and (m.archived is false)
        """)
    Optional<Member> findByIdWhereArchivedFalse(Long id);
}

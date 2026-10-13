package com.wikt0r2115.library.infrastructure;

import com.wikt0r2115.library.domain.Book;
import com.wikt0r2115.library.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query(value = "SELECT member_id FROM member WHERE member_id = :id FOR UPDATE", nativeQuery = true)
    Optional<Long> lockById(@Param("id") Long id);

    @Query("""
            select m from Member m
                    where (m.memberId = :id)
                        and (m.archived is false)
        """)
    Optional<Member> findByIdWhereArchivedFalse(Long id);
}

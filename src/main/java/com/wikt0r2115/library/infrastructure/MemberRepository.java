package com.wikt0r2115.library.infrastructure;

import com.wikt0r2115.library.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> { }

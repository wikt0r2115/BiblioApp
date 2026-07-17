package com.wikt0r2115.library.infrastructure;

import com.wikt0r2115.library.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> { }

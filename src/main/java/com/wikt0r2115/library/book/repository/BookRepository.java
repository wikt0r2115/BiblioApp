package com.wikt0r2115.library.book.repository;

import com.wikt0r2115.library.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> { }

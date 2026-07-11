package com.wikt0r2115.library.infrastructure;

import com.wikt0r2115.library.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("""
            select b from Book b
            where (:author is null or lower(b.author) like lower(concat('%', :author, '%')))
              and (:title is null or lower(b.title) like lower(concat('%', :title, '%')))
              and (:available is null or b.available = :available)
            """)
    Page<Book> findWithFilters(
            @Param("author") String author,
            @Param("title") String title,
            @Param("available") Boolean available,
            Pageable pageable);
}

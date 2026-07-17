package com.wikt0r2115.library.infrastructure;

import com.wikt0r2115.library.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(attributePaths = {"author", "categories"})
    Optional<Book> findById(Long id);

    @EntityGraph(attributePaths = {"author", "categories"})
    @Query("""
            select distinct b from Book b
            where (:authorId is null or b.author.authorId = :authorId)
              and (:title is null or lower(b.title) like lower(concat('%', :title, '%')))
              and (:available is null or b.available = :available)
            """)
    Page<Book> findWithFilters(
            @Param("authorId") Long authorId,
            @Param("title") String title,
            @Param("available") Boolean available,
            Pageable pageable);
}

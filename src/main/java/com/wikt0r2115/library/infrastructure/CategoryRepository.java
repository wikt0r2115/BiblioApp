package com.wikt0r2115.library.infrastructure;

import com.wikt0r2115.library.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> { }

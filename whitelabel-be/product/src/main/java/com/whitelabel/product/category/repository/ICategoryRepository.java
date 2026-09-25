package com.whitelabel.product.category.repository;

import com.whitelabel.product.category.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ICategoryRepository extends JpaRepository<Category, UUID> {
    Page<Category> findByParentIsNull(Pageable pageable);

    Page<Category> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}

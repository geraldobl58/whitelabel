package com.whitelabel.product.category.repository;

import com.whitelabel.product.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ICategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByParentIsNull();
}

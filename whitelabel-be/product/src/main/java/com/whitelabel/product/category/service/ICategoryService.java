package com.whitelabel.product.category.service;

import com.whitelabel.product.category.dto.CategoryRequestDTO;
import com.whitelabel.product.category.dto.CategoryResponseDTO;
import com.whitelabel.product.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ICategoryService {
    CategoryResponseDTO create(CategoryRequestDTO requestDTO);
    PageResponseDTO<CategoryResponseDTO> findAll(String title, Pageable pageable);
    CategoryResponseDTO findById(UUID id);
    CategoryResponseDTO findBySlug(String slug);
    CategoryResponseDTO update(UUID id, CategoryRequestDTO requestDTO);
    void delete(UUID id);
}

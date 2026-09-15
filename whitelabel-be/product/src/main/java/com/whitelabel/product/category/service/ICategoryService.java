package com.whitelabel.product.category.service;

import com.whitelabel.product.category.dto.CategoryRequestDTO;
import com.whitelabel.product.category.dto.CategoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ICategoryService {
    CategoryResponseDTO create(CategoryRequestDTO requestDTO);
    List<CategoryResponseDTO> findAll();
    CategoryResponseDTO findById(UUID id);
    CategoryResponseDTO update(UUID id, CategoryRequestDTO requestDTO);
    void delete(UUID id);
}

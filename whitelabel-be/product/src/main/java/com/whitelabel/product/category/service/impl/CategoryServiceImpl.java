package com.whitelabel.product.category.service.impl;

import com.whitelabel.product.category.dto.CategoryRequestDTO;
import com.whitelabel.product.category.dto.CategoryResponseDTO;
import com.whitelabel.product.category.mapper.ICategoryMapper;
import com.whitelabel.product.category.model.Category;
import com.whitelabel.product.category.repository.ICategoryRepository;
import com.whitelabel.product.category.service.ICategoryService;
import com.whitelabel.product.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {
    private final ICategoryRepository categoryRepository;
    private final ICategoryMapper categoryMapper;

    @Override
    public CategoryResponseDTO create(CategoryRequestDTO requestDTO) {
        Category category = buildTree(requestDTO, null);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponseDTO(savedCategory);
    }

    @Override
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(categoryMapper::toResponseDTO)
                .toList();
    }

    @Override
    public CategoryResponseDTO findById(UUID id) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return categoryMapper.toResponseDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(UUID id, CategoryRequestDTO requestDTO) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setTitle(requestDTO.title());

        category.getSubcategories().clear();
        category.getSubcategories().addAll(buildChildren(requestDTO.subcategories(), category));

        Category updateCategory = categoryRepository.save(category);

        return categoryMapper.toResponseDTO(updateCategory);
    }

    @Override
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }

        categoryRepository.deleteById(id);
    }

    private Category buildTree(CategoryRequestDTO requestDTO, Category parent) {
        Category category = Category.builder()
                .title(requestDTO.title())
                .parent(parent)
                .build();

        category.getSubcategories().addAll(buildChildren(requestDTO.subcategories(), category));

        return category;
    }

    private List<Category> buildChildren(List<CategoryRequestDTO> subcategories, Category parent) {
        if (subcategories == null) {
            return new ArrayList<>();
        }

        return subcategories.stream()
                .map(subcategory -> buildTree(subcategory, parent))
                .toList();
    }
}

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
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        reconcileChildren(category, requestDTO.subcategories());

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

    /**
     * Matches each item in {@code requests} against the parent's current subcategories by id:
     * existing ids are updated in place (recursively), items without a matching id are created,
     * and any current child whose id is absent from {@code requests} is dropped from the
     * collection (deleted via orphanRemoval on save).
     */
    private void reconcileChildren(Category parent, List<CategoryRequestDTO> requests) {
        List<CategoryRequestDTO> childRequests = requests == null ? List.of() : requests;

        Map<UUID, Category> existingById = parent.getSubcategories().stream()
                .filter(child -> child.getId() != null)
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        List<Category> reconciled = new ArrayList<>();
        for (CategoryRequestDTO request : childRequests) {
            Category child = request.id() != null ? existingById.get(request.id()) : null;

            if (child == null) {
                child = buildTree(request, parent);
            } else {
                child.setTitle(request.title());
                reconcileChildren(child, request.subcategories());
            }

            reconciled.add(child);
        }

        parent.getSubcategories().clear();
        parent.getSubcategories().addAll(reconciled);
    }
}

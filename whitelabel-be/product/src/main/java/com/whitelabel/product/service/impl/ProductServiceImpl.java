package com.whitelabel.product.service.impl;

import com.whitelabel.product.category.model.Category;
import com.whitelabel.product.category.repository.ICategoryRepository;
import com.whitelabel.product.exception.ResourceNotFoundException;
import com.whitelabel.product.dto.PageResponseDTO;
import com.whitelabel.product.dto.ProductFilterDTO;
import com.whitelabel.product.dto.ProductRequestDTO;
import com.whitelabel.product.dto.ProductResponseDTO;
import com.whitelabel.product.mapper.IProductMapper;
import com.whitelabel.product.model.Product;
import com.whitelabel.product.repository.IProductRepository;
import com.whitelabel.product.repository.ProductSpecifications;
import com.whitelabel.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {
    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    private final IProductMapper productMapper;

    @Override
    public ProductResponseDTO create(ProductRequestDTO requestDTO) {
        Product product = productMapper.toEntity(requestDTO);
        product.setCategory(findCategory(requestDTO.categoryId()));

        Product saveProduct = productRepository.save(product);

        return productMapper.toResponseDTO(saveProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponseDTO> findAll(ProductFilterDTO filter, Pageable pageable) {
        Collection<UUID> categoryIds = resolveCategoryIds(filter);

        return PageResponseDTO.from(
                productRepository
                        .findAll(ProductSpecifications.withFilters(filter, categoryIds), pageable)
                        .map(productMapper::toResponseDTO)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(UUID id) {
       Product product = productRepository
               .findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id)
       );

       return productMapper.toResponseDTO(product);
    }

    @Override
    public ProductResponseDTO update( UUID id, ProductRequestDTO requestDTO) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productMapper.updateProductFromRequest(requestDTO, product);
        product.setCategory(findCategory(requestDTO.categoryId()));

        Product updateProduct = productRepository.save(product);

        return productMapper.toResponseDTO(updateProduct);

    }

    @Override
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
    }

    private Category findCategory(UUID categoryId) {
        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    /**
     * Returns null when no category filter was sent, so the specification skips the IN clause entirely.
     */
    private Collection<UUID> resolveCategoryIds(ProductFilterDTO filter) {
        if (filter.categoryId() == null && !StringUtils.hasText(filter.categorySlug())) {
            return null;
        }

        Category category = filter.categoryId() != null
                ? findCategory(filter.categoryId())
                : categoryRepository
                        .findBySlug(filter.categorySlug())
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", filter.categorySlug()));

        if (!filter.shouldIncludeSubcategories()) {
            return Set.of(category.getId());
        }

        Set<UUID> ids = new LinkedHashSet<>();
        Deque<Category> pending = new ArrayDeque<>();
        pending.add(category);

        while (!pending.isEmpty()) {
            Category current = pending.poll();

            if (ids.add(current.getId())) {
                pending.addAll(current.getSubcategories());
            }
        }

        return ids;
    }
}

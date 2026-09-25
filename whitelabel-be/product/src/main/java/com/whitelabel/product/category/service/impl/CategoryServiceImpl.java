package com.whitelabel.product.category.service.impl;

import com.whitelabel.product.category.dto.CategoryRequestDTO;
import com.whitelabel.product.category.dto.CategoryResponseDTO;
import com.whitelabel.product.category.mapper.ICategoryMapper;
import com.whitelabel.product.category.model.Category;
import com.whitelabel.product.category.repository.ICategoryRepository;
import com.whitelabel.product.category.service.ICategoryService;
import com.whitelabel.product.dto.PageResponseDTO;
import com.whitelabel.product.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_SLUG_CHARS = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-|-$)");
    private static final String FALLBACK_SLUG = "categoria";

    private final ICategoryRepository categoryRepository;
    private final ICategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO requestDTO) {
        Category category = buildTree(requestDTO, null, new HashSet<>());

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponseDTO(savedCategory);
    }

    /**
     * Without a title, lists root categories only — each one already carries its whole subtree, so
     * including descendants as top-level rows would repeat them. A title searches every level instead,
     * returning each match with the subtree hanging below it.
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CategoryResponseDTO> findAll(String title, Pageable pageable) {
        Page<Category> categories = StringUtils.hasText(title)
                ? categoryRepository.findByTitleContainingIgnoreCase(title, pageable)
                : categoryRepository.findByParentIsNull(pageable);

        return PageResponseDTO.from(categories.map(categoryMapper::toResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(UUID id) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return categoryMapper.toResponseDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findBySlug(String slug) {
        Category category = categoryRepository
                .findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));

        return categoryMapper.toResponseDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(UUID id, CategoryRequestDTO requestDTO) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        Set<String> takenSlugs = new HashSet<>();

        category.setTitle(requestDTO.title());
        applyRequestedSlug(category, requestDTO, takenSlugs);
        reconcileChildren(category, requestDTO.subcategories(), takenSlugs);

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

    private Category buildTree(CategoryRequestDTO requestDTO, Category parent, Set<String> takenSlugs) {
        Category category = Category.builder()
                .title(requestDTO.title())
                .slug(resolveSlug(requestDTO.slug(), requestDTO.title(), null, takenSlugs))
                .parent(parent)
                .build();

        category.getSubcategories().addAll(buildChildren(requestDTO.subcategories(), category, takenSlugs));

        return category;
    }

    private List<Category> buildChildren(List<CategoryRequestDTO> subcategories, Category parent, Set<String> takenSlugs) {
        if (subcategories == null) {
            return new ArrayList<>();
        }

        return subcategories.stream()
                .map(subcategory -> buildTree(subcategory, parent, takenSlugs))
                .toList();
    }

    /**
     * Matches each item in {@code requests} against the parent's current subcategories by id:
     * existing ids are updated in place (recursively), items without a matching id are created,
     * and any current child whose id is absent from {@code requests} is dropped from the
     * collection (deleted via orphanRemoval on save).
     */
    private void reconcileChildren(Category parent, List<CategoryRequestDTO> requests, Set<String> takenSlugs) {
        List<CategoryRequestDTO> childRequests = requests == null ? List.of() : requests;

        Map<UUID, Category> existingById = parent.getSubcategories().stream()
                .filter(child -> child.getId() != null)
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        List<Category> reconciled = new ArrayList<>();
        for (CategoryRequestDTO request : childRequests) {
            Category child = request.id() != null ? existingById.get(request.id()) : null;

            if (child == null) {
                child = buildTree(request, parent, takenSlugs);
            } else {
                child.setTitle(request.title());
                applyRequestedSlug(child, request, takenSlugs);
                reconcileChildren(child, request.subcategories(), takenSlugs);
            }

            reconciled.add(child);
        }

        parent.getSubcategories().clear();
        parent.getSubcategories().addAll(reconciled);
    }

    /**
     * A rename on its own never rewrites the slug — published URLs would break. The slug only moves
     * when the request carries an explicit, different one.
     */
    private void applyRequestedSlug(Category category, CategoryRequestDTO requestDTO, Set<String> takenSlugs) {
        if (!StringUtils.hasText(requestDTO.slug())) {
            return;
        }

        String requested = toSlug(requestDTO.slug());
        if (requested.equals(category.getSlug())) {
            return;
        }

        category.setSlug(resolveSlug(requested, category.getTitle(), category.getId(), takenSlugs));
    }

    /**
     * @param excludedId the category being updated, so its own stored slug doesn't count as a collision
     * @param takenSlugs slugs already handed out earlier in this same request, which aren't persisted yet
     */
    private String resolveSlug(String requestedSlug, String title, UUID excludedId, Set<String> takenSlugs) {
        String base = toSlug(StringUtils.hasText(requestedSlug) ? requestedSlug : title);

        if (base.isEmpty()) {
            base = FALLBACK_SLUG;
        }

        String candidate = base;
        int suffix = 2;
        while (takenSlugs.contains(candidate) || isSlugTaken(candidate, excludedId)) {
            candidate = base + "-" + suffix++;
        }

        takenSlugs.add(candidate);

        return candidate;
    }

    private boolean isSlugTaken(String slug, UUID excludedId) {
        return excludedId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, excludedId);
    }

    private static String toSlug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        String withoutAccents = DIACRITICS.matcher(normalized).replaceAll("");
        String dashed = NON_SLUG_CHARS.matcher(withoutAccents.toLowerCase(Locale.ROOT)).replaceAll("-");

        return EDGE_DASHES.matcher(dashed).replaceAll("");
    }
}

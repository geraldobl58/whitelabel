package com.whitelabel.product.repository;

import com.whitelabel.product.dto.ProductFilterDTO;
import com.whitelabel.product.model.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> withFilters(ProductFilterDTO filter, Collection<UUID> categoryIds) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.title())) {
                predicates.add(builder.like(builder.lower(root.get("title")), contains(filter.title())));
            }

            if (StringUtils.hasText(filter.brand())) {
                predicates.add(builder.like(builder.lower(root.get("brand")), contains(filter.brand())));
            }

            if (StringUtils.hasText(filter.status())) {
                predicates.add(builder.equal(builder.upper(root.get("status")), filter.status().toUpperCase()));
            }

            if (filter.minPrice() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("price").get("originalPrice"), filter.minPrice()));
            }

            if (filter.maxPrice() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("price").get("originalPrice"), filter.maxPrice()));
            }

            if (categoryIds != null) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String contains(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}

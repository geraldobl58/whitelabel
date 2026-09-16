package com.whitelabel.product.mapper;

import com.whitelabel.product.category.dto.CategorySummaryDTO;
import com.whitelabel.product.category.model.Category;
import com.whitelabel.product.dto.ProductRequestDTO;
import com.whitelabel.product.dto.ProductResponseDTO;
import com.whitelabel.product.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Mapper(componentModel = "spring")
public interface IProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(ProductRequestDTO requestDTO);

    ProductResponseDTO toResponseDTO(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateProductFromRequest(ProductRequestDTO productRequest, @MappingTarget Product product);

    default CategorySummaryDTO toCategorySummary(Category category) {
        if (category == null) {
            return null;
        }

        Deque<String> path = new ArrayDeque<>();
        for (Category current = category; current != null; current = current.getParent()) {
            path.addFirst(current.getTitle());
        }

        return new CategorySummaryDTO(category.getId(), category.getTitle(), category.getSlug(), List.copyOf(path));
    }
}

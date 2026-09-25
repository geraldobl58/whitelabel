package com.whitelabel.product.category.mapper;

import com.whitelabel.product.category.dto.CategoryResponseDTO;
import com.whitelabel.product.category.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ICategoryMapper {
    CategoryResponseDTO toResponseDTO(Category category);
}

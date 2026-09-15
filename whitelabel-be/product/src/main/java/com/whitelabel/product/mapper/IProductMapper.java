package com.whitelabel.product.mapper;

import com.whitelabel.product.dto.ProductRequestDTO;
import com.whitelabel.product.dto.ProductResponseDTO;
import com.whitelabel.product.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IProductMapper {

    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequestDTO requestDTO);

    ProductResponseDTO toResponseDTO(Product product);

    @Mapping(target = "id", ignore = true)
    void updateProductFromRequest(ProductRequestDTO productRequest, @MappingTarget Product product);
}

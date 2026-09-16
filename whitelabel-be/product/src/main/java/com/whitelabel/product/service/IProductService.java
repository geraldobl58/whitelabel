package com.whitelabel.product.service;

import com.whitelabel.product.dto.PageResponseDTO;
import com.whitelabel.product.dto.ProductFilterDTO;
import com.whitelabel.product.dto.ProductRequestDTO;
import com.whitelabel.product.dto.ProductResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IProductService {
    ProductResponseDTO create(ProductRequestDTO requestDTO);
    PageResponseDTO<ProductResponseDTO> findAll(ProductFilterDTO filter, Pageable pageable);
    ProductResponseDTO findById(UUID id);
    ProductResponseDTO update(UUID id, ProductRequestDTO requestDTO);
    void delete(UUID id);
}

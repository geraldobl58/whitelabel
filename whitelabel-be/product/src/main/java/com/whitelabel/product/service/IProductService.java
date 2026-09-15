package com.whitelabel.product.service;

import com.whitelabel.product.dto.ProductRequestDTO;
import com.whitelabel.product.dto.ProductResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IProductService {
    ProductResponseDTO create(ProductRequestDTO requestDTO);
    List<ProductResponseDTO> findAll();
    ProductResponseDTO findById(UUID id);
    ProductResponseDTO update(UUID id, ProductRequestDTO requestDTO);
    void delete(UUID id);
}

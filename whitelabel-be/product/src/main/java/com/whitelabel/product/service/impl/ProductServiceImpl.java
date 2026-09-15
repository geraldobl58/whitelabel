package com.whitelabel.product.service.impl;

import com.whitelabel.product.exception.ResourceNotFoundException;
import com.whitelabel.product.dto.ProductRequestDTO;
import com.whitelabel.product.dto.ProductResponseDTO;
import com.whitelabel.product.mapper.IProductMapper;
import com.whitelabel.product.model.Product;
import com.whitelabel.product.repository.IProductRepository;
import com.whitelabel.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {
    private final IProductRepository productRepository;
    private final IProductMapper productMapper;

    @Override
    public ProductResponseDTO create(ProductRequestDTO requestDTO) {
        Product product = productMapper.toEntity(requestDTO);

        Product saveProduct = productRepository.save(product);

        return productMapper.toResponseDTO(saveProduct);
    }

    @Override
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDTO)
                .toList();
    }

    @Override
    public ProductResponseDTO findById(UUID id) {
       Product product = productRepository
               .findById(id)
               .orElseThrow(
               () -> new ResourceNotFoundException("Product", "id", id)
       );

       return productMapper.toResponseDTO(product);
    }

    @Override
    public ProductResponseDTO update( UUID id, ProductRequestDTO requestDTO) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product", "id", id)
                );

        productMapper.updateProductFromRequest(requestDTO, product);

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
}

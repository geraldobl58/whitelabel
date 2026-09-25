package com.whitelabel.stock.service;

import com.whitelabel.stock.dto.PageResponseDTO;
import com.whitelabel.stock.dto.StockRequestDTO;
import com.whitelabel.stock.dto.StockResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IStockService {
    boolean isInStock(String sku, Integer quantity);
    StockResponseDTO create(StockRequestDTO stockRequestDTO);
    PageResponseDTO<StockResponseDTO> findAll(Pageable pageable, String sku, Integer quantity);
    StockResponseDTO update(UUID id, StockRequestDTO stockRequestDTO);
    void delete(UUID id);
    StockResponseDTO reduceStock(String sku, Integer quantity);
}

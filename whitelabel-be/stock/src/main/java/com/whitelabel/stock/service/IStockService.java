package com.whitelabel.stock.service;

import com.whitelabel.stock.dto.StockRequestDTO;
import com.whitelabel.stock.dto.StockResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IStockService {
    boolean isInStock(String sku, Integer quantity);
    StockResponseDTO create(StockRequestDTO stockRequestDTO);
    List<StockResponseDTO> findAll();
    StockResponseDTO update(UUID id, StockRequestDTO stockRequestDTO);
    void delete(UUID id);
}

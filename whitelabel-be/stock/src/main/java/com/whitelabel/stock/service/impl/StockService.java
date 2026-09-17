package com.whitelabel.stock.service.impl;

import com.whitelabel.stock.dto.StockRequestDTO;
import com.whitelabel.stock.dto.StockResponseDTO;
import com.whitelabel.stock.exception.ResourceNotFoundException;
import com.whitelabel.stock.mapper.IStockMapper;
import com.whitelabel.stock.model.Stock;
import com.whitelabel.stock.repository.IStockRepository;
import com.whitelabel.stock.service.IStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService implements IStockService {
    private final IStockRepository stockRepository;
    private final IStockMapper stockMapper;

    @Override
    @Transactional(readOnly = true)
    public boolean isInStock(String sku, Integer quantity) {
        return stockRepository.findBySku(sku)
                .map(stock -> stock.getQuantity() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional
    public StockResponseDTO create(StockRequestDTO stockRequestDTO) {
        boolean exists = stockRepository.existsBySku(stockRequestDTO.getSku());

        if (exists) {
           throw new RuntimeException("Stock for sku " + stockRequestDTO.getSku() + "already exists");
        }

        Stock stock = stockMapper.toEntity(stockRequestDTO);
        Stock savedStock = stockRepository.save(stock);

        log.info("Stock create for SKU: {}", savedStock.getSku());

        return stockMapper.toResponse(savedStock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponseDTO> findAll() {
        return stockRepository.findAll()
                .stream()
                .map(stockMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional()
    public StockResponseDTO update(UUID id, StockRequestDTO stockRequestDTO) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Stock", "id", id)
                );

        stock.setSku(stockRequestDTO.getSku());
        stock.setQuantity(stockRequestDTO.getQuantity());

        log.info("Stock update for SKU: {}", stock.getSku());

        return stockMapper.toResponse(stockRepository.save(stock));
    }

    @Override
    @Transactional()
    public void delete(UUID id) {
        if (!stockRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stock", "id", id);
        }

        stockRepository.deleteById(id);

        log.info("Stock delete for SKU: {}", id);
    }
}

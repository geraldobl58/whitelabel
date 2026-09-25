package com.whitelabel.stock.service.impl;

import com.whitelabel.stock.dto.PageResponseDTO;
import com.whitelabel.stock.dto.StockRequestDTO;
import com.whitelabel.stock.dto.StockResponseDTO;
import com.whitelabel.stock.exception.ResourceNotFoundException;
import com.whitelabel.stock.mapper.IStockMapper;
import com.whitelabel.stock.model.Stock;
import com.whitelabel.stock.repository.IStockRepository;
import com.whitelabel.stock.service.IStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService implements IStockService {
    private final IStockRepository stockRepository;
    private final IStockMapper stockMapper;

    @Value("${stock.allow-backorders:false}")
    private boolean allowBackorders;

    @Override
    @Transactional(readOnly = true)
    public boolean isInStock(String sku, Integer quantity) {
        if (allowBackorders) {
            log.warn("MODE BACKORDERS ACTIVE: Authorized stock for SKU: {}", sku);
            return true;
        }

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
    public PageResponseDTO<StockResponseDTO> findAll(Pageable pageable, String sku, Integer quantity) {
        Page<Stock> page;
        if (sku != null && quantity != null) {
            page = stockRepository.findBySkuAndQuantityGreaterThanEqual(sku, quantity, pageable);
        } else if (sku != null) {
            page = stockRepository.findBySku(sku, pageable);
        } else if (quantity != null) {
            page = stockRepository.findByQuantityGreaterThanEqual(quantity, pageable);
        } else {
            page = stockRepository.findAll(pageable);
        }

        return PageResponseDTO.from(page.map(stockMapper::toResponse));
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

    @Override
    @Transactional
    public StockResponseDTO reduceStock(String sku, Integer quantity) {
        Stock stock = stockRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "sku", sku));

        if (stock.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Stock for sku " + sku + " is lower than requested quantity " + quantity);
        }

        stock.setQuantity(stock.getQuantity() - quantity);

        log.info("Stock reduced for SKU: {} by {}", sku, quantity);

        return stockMapper.toResponse(stockRepository.save(stock));
    }
}

package com.whitelabel.stock.repository;

import com.whitelabel.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IStockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findBySku(String sku);
    boolean existsBySku(String sku);
}

package com.whitelabel.product.repository;

import com.whitelabel.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface IProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
}

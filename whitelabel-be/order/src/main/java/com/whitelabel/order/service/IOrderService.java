package com.whitelabel.order.service;

import com.whitelabel.order.dto.OrderRequestDTO;
import com.whitelabel.order.dto.OrderResponseDTO;
import com.whitelabel.order.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IOrderService {
    CompletableFuture<OrderResponseDTO> create(OrderRequestDTO orderRequestDTO, String userId);
    PageResponseDTO<OrderResponseDTO> findAll(Pageable pageable, String userId, boolean isAdmin);
    OrderResponseDTO findById(UUID id);
    void delete(UUID id);
}

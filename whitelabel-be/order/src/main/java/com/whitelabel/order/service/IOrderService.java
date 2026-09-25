package com.whitelabel.order.service;

import com.whitelabel.order.dto.OrderRequestDTO;
import com.whitelabel.order.dto.OrderResponseDTO;
import com.whitelabel.order.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IOrderService {
    OrderResponseDTO create(OrderRequestDTO orderRequestDTO, String userId);
//    PageResponseDTO<OrderResponseDTO> findAll(Pageable pageable);
    PageResponseDTO<OrderResponseDTO> findAll(Pageable pageable, String userId, boolean isAdmin);
    OrderResponseDTO findById(UUID id);
    void delete(UUID id);
}

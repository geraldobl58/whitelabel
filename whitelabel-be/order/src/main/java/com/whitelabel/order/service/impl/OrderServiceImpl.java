package com.whitelabel.order.service.impl;

import com.whitelabel.order.dto.OrderRequestDTO;
import com.whitelabel.order.dto.OrderResponseDTO;
import com.whitelabel.order.dto.PageResponseDTO;
import com.whitelabel.order.exception.ResourceNotFoundException;
import com.whitelabel.order.mapper.IOrderMapper;
import com.whitelabel.order.model.Order;
import com.whitelabel.order.repository.OrderRepository;
import com.whitelabel.order.service.IOrderService;
import com.whitelabel.order.service.client.IStockClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@RefreshScope
public class OrderServiceImpl implements IOrderService {
    private final OrderRepository orderRepository;
    private final IOrderMapper orderMapper;
    private final IStockClient stockClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Value("${order.enabled:true}")
    private boolean ordersEnabled;

    @Override
    @Transactional
    public OrderResponseDTO create(OrderRequestDTO orderRequestDTO, String userId) {
        if (!ordersEnabled) {
            log.warn("Order error: The service disabled for configuration");
            throw new RuntimeException("The service of order it's a maintenance");
        }

        log.info("Creating new order with {} item(s)", orderRequestDTO.getOrderItemList().size());

        Order order = orderMapper.toOrder(orderRequestDTO);

        order.setUserId(userId);

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("stock");

        for(var item : order.getOrderItems()) {
            String sku = item.getSku();
            Integer quantity = item.getQuantity();

            try {
                circuitBreaker.executeRunnable(() -> stockClient.reduceStock(sku, quantity));
            } catch (CallNotPermittedException ex) {
                log.error("Stock service circuit breaker is open, SKU {}: {}", sku, ex.getMessage());
                throw new IllegalArgumentException("Stock service is currently unavailable, try again later!", ex);
            } catch (WebClientResponseException.NotFound ex) {
                log.error("Product not found for SKU {}: {}", sku, ex.getMessage());
                throw new IllegalArgumentException("Product not found to place the order!", ex);
            } catch (WebClientResponseException ex) {
                log.error("Inventory API error during reduction SKU {}: Status {} - {}", sku, ex.getStatusCode(), ex.getResponseBodyAsString());
                throw new IllegalArgumentException("Error processing inventory: " + ex.getResponseBodyAsString(), ex);
            } catch (Exception ex) {
                log.error("Unexpected error while creating the order {}: {}", sku, ex.getMessage());
                throw new IllegalArgumentException("Error processing your request!", ex);
            }
        }

        order.setOrderNumber(UUID.randomUUID().toString());

        Order savedOrder = orderRepository.save(order);

        log.info("Order saved with ID: {}", savedOrder.getId());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponseDTO> findAll(Pageable pageable, String userId, boolean isAdmin) {
        Page<Order> orders = isAdmin
                ? orderRepository.findAll(pageable)
                : orderRepository.findByUserId(userId, pageable);

        return PageResponseDTO.from(orders.map(orderMapper::toOrderResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO findById(UUID id) {
        Order order = orderRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Order", "id", id)
                );

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", "id", id);
        }

        orderRepository.deleteById(id);

        log.info("Order deleted with ID: {}", id);
    }
}

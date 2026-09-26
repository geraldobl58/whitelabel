package com.whitelabel.stock.listner;

import com.whitelabel.stock.event.OrderPlacedEvent;
import com.whitelabel.stock.service.impl.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class OrderEventsListener {

    private final StockService stockService;

    @RabbitListener(queues = "stock-queue")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("🚀Received order placed event {}", event.orderNumber());

        event.items().forEach(
                item -> {
                    try {
                        stockService.reduceStock(item.sku(), item.quantity());
                    } catch (Exception ex) {
                       log.error("😰Error handling order placed event for SKU {}: {}", item.sku(), ex.getMessage());
                    }
                }
        );
    }
}

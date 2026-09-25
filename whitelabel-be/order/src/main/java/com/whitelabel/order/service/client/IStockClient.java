package com.whitelabel.order.service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PutExchange;

public interface IStockClient {

    @PutExchange("/api/v1/stocks/{sku}/reduce")
    void reduceStock(@PathVariable String sku, @RequestParam Integer quantity);
}

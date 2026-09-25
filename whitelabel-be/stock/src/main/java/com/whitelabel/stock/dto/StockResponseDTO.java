package com.whitelabel.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class StockResponseDTO {
    private UUID id;
    private String sku;
    private Integer quantity;
    private boolean status;
}

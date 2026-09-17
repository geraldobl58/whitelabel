package com.whitelabel.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockRequestDTO {

    @NotBlank(message = "Sku is required")
    private String sku;

    @Min(value = 0, message = "Quantity is not -zero")
    private Integer quantity;
}

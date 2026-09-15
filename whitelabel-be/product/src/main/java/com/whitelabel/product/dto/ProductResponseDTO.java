package com.whitelabel.product.dto;

import com.whitelabel.product.model.Price;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String title,
        String brand,
        String image,
        List<String> gallery,
        String description,
        String status,
        Price price,
        UUID categoryId,
        LocalDateTime createdAt
) {
}

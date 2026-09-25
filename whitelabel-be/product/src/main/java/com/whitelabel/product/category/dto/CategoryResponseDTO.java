package com.whitelabel.product.category.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        String title,
        String slug,
        List<CategoryResponseDTO> subcategories,
        LocalDateTime createdAt
) {
}

package com.whitelabel.product.category.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record CategoryRequestDTO(
        UUID id,

        @NotBlank(message = "Title is required")
        String title,

        @Valid
        List<CategoryRequestDTO> subcategories
) {
}

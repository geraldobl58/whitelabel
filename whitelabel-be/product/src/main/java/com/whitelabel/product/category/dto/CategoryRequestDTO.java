package com.whitelabel.product.category.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CategoryRequestDTO(
        @NotBlank(message = "Title is required")
        String title,

        @Valid
        List<CategoryRequestDTO> subcategories
) {
}

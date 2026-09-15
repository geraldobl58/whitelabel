package com.whitelabel.product.dto;

import com.whitelabel.product.model.Price;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductRequestDTO(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Brand is required")
        String brand,

        @NotBlank(message = "Image is required")
        String image,

        List<String> gallery,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Status is required")
        String status,

        @NotNull(message = "Price is required")
        @Valid
        Price price
) {
}

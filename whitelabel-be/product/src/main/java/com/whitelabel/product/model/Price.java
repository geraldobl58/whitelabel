package com.whitelabel.product.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price {
    @Column(name = "original_price", nullable = false)
    @NotNull(message = "Original price is required")
    @DecimalMin(value = "0.01", message = "Original price must be greater than zero")
    private BigDecimal originalPrice;

    @Column(name = "discount_price")
    @DecimalMin(value = "0.00", inclusive = true, message = "Discount price must not be negative")
    private BigDecimal discountPrice;

    @Column(name = "discount_percentage")
    @PositiveOrZero(message = "Discount percentage must be zero or positive")
    private Integer discountPercentage;
}

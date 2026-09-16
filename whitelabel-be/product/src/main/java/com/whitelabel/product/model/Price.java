package com.whitelabel.product.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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

    /**
     * Derived from originalPrice/discountPrice, never stored or accepted from a client -
     * the two source values are the single source of truth.
     */
    public Integer getDiscountPercentage() {
        if (originalPrice == null || originalPrice.signum() == 0 || discountPrice == null) {
            return 0;
        }

        BigDecimal discount = originalPrice.subtract(discountPrice);
        if (discount.signum() <= 0) {
            return 0;
        }

        return discount
                .multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}

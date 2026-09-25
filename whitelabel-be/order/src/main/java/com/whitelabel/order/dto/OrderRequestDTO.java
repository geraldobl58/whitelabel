package com.whitelabel.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {
    @Valid
    @NotEmpty(message = "Order should be contain a item")
    private List<OrderItemRequestDTO> orderItemList;
}

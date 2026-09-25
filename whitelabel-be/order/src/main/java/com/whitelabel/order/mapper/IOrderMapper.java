package com.whitelabel.order.mapper;

import com.whitelabel.order.dto.OrderItemRequestDTO;
import com.whitelabel.order.dto.OrderItemResponseDTO;
import com.whitelabel.order.dto.OrderRequestDTO;
import com.whitelabel.order.dto.OrderResponseDTO;
import com.whitelabel.order.model.Order;
import com.whitelabel.order.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "orderItems", source = "orderItemList")
    Order toOrder(OrderRequestDTO orderRequestDTO);

    @Mapping(target = "id", ignore = true)
    OrderItem toOrderItem(OrderItemRequestDTO orderItemRequestDTO);

    @Mapping(target = "orderItems", source = "orderItems")
    OrderResponseDTO toOrderResponse(Order order);

    OrderItemResponseDTO toOrderItemResponse(OrderItem orderItem);
}

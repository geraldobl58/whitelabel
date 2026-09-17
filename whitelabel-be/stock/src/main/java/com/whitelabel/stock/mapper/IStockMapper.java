package com.whitelabel.stock.mapper;

import com.whitelabel.stock.dto.StockRequestDTO;
import com.whitelabel.stock.dto.StockResponseDTO;
import com.whitelabel.stock.model.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IStockMapper {
    Stock toEntity(StockRequestDTO stockRequestDTO);

    @Mapping(target = "status", expression = "java(stock.getQuantity() > 0)")
    StockResponseDTO toResponse(Stock stock);
}

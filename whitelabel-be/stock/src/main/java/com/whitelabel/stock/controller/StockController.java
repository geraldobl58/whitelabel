package com.whitelabel.stock.controller;

import com.whitelabel.stock.dto.StockRequestDTO;
import com.whitelabel.stock.dto.StockResponseDTO;
import com.whitelabel.stock.service.impl.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Products", description = "Product catalog. Every product belongs to exactly one category or subcategory via categoryId.")
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
class StockController {
    private final StockService stockService;

    @Operation(
            summary = "Get stock by sku",
            description = ""
    )
    @GetMapping("/{sku}")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@PathVariable("sku") String sku, @RequestParam("quantity") Integer quantity) {
        return stockService.isInStock(sku, quantity);
    }

    @Operation(
            summary = "Create a stock",
            description = "Create a new stock"
    )
    @ApiResponse(responseCode = "201", description = "Stock created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponseDTO create(@Valid @RequestBody StockRequestDTO stockRequestDTO) {
        return stockService.create(stockRequestDTO);
    }

    @Operation(
            summary = "Get all stock",
            description = ""
    )
    @ApiResponse(responseCode = "200", description = "Find all stocks")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<StockResponseDTO> findAll() {
        return stockService.findAll();
    }

    @Operation(
            summary = "Update a stock",
            description = ""
    )
    @ApiResponse(responseCode = "200", description = "Stock updated")
    @ApiResponse(responseCode = "404", description = "Stock not found")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StockResponseDTO update(@PathVariable UUID id, @Valid @RequestBody StockRequestDTO stockRequestDTO) {
        return stockService.update(id, stockRequestDTO);
    }

    @Operation(summary = "Delete a stock")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "Stock not found")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        stockService.delete(id);
    }
}

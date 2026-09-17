package com.whitelabel.stock.controller;

import com.whitelabel.stock.dto.PageResponseDTO;
import com.whitelabel.stock.dto.StockRequestDTO;
import com.whitelabel.stock.dto.StockResponseDTO;
import com.whitelabel.stock.service.impl.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Stocks",
        description = "Inventory levels per SKU. Each stock record holds the quantity currently available "
                + "for one SKU, and reports a derived `status` flag (true when quantity is above zero)."
)
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
class StockController {

    private static final String STOCK_EXAMPLE = """
            {
              "sku": "REDMI-18-BLACK-128",
              "quantity": 25
            }
            """;

    private final StockService stockService;

    @Operation(
            summary = "Check whether a SKU has enough stock",
            description = "Returns true when the SKU exists and its quantity is greater than or equal to the "
                    + "requested `quantity`. An unknown SKU returns false, not 404 — this endpoint answers "
                    + "'can I fulfil this?', not 'does this record exist?'."
    )
    @ApiResponse(responseCode = "200", description = "Availability answer")
    @GetMapping("/{sku}")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(
            @Parameter(description = "SKU to check", example = "REDMI-18-BLACK-128")
            @PathVariable("sku") String sku,
            @Parameter(description = "How many units are needed", example = "5")
            @RequestParam("quantity") Integer quantity) {
        return stockService.isInStock(sku, quantity);
    }

    @Operation(
            summary = "Create a stock record",
            description = "Registers the available quantity for a SKU. The `status` field in the response is "
                    + "derived from the quantity, never sent by the client."
    )
    @ApiResponse(responseCode = "201", description = "Stock created")
    @ApiResponse(responseCode = "400", description = "Validation error (e.g. blank SKU or negative quantity)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponseDTO create(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "SKU and its available quantity", required = true,
                    content = @Content(schema = @Schema(implementation = StockRequestDTO.class),
                            examples = @ExampleObject(name = "New stock", value = STOCK_EXAMPLE)))
            @RequestBody StockRequestDTO stockRequestDTO) {
        return stockService.create(stockRequestDTO);
    }

    @Operation(
            summary = "List stock records (paginated)",
            description = "Pages are 10 items by default: `?page=0&size=10&sort=sku,asc`. "
                    + "The page payload comes under `data`, alongside the paging metadata."
    )
    @ApiResponse(responseCode = "200", description = "Page of stock records")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<StockResponseDTO> findAll(@ParameterObject Pageable pageable) {
        return stockService.findAll(pageable);
    }

    @Operation(
            summary = "Update a stock record",
            description = "Replaces both SKU and quantity of an existing record."
    )
    @ApiResponse(responseCode = "200", description = "Stock updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Stock not found")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StockResponseDTO update(
            @PathVariable UUID id,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New SKU and quantity", required = true,
                    content = @Content(schema = @Schema(implementation = StockRequestDTO.class),
                            examples = @ExampleObject(name = "Update stock", value = STOCK_EXAMPLE)))
            @RequestBody StockRequestDTO stockRequestDTO) {
        return stockService.update(id, stockRequestDTO);
    }

    @Operation(summary = "Delete a stock record")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "Stock not found")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        stockService.delete(id);
    }
}

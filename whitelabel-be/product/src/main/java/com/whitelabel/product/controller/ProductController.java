package com.whitelabel.product.controller;

import com.whitelabel.product.dto.ProductRequestDTO;
import com.whitelabel.product.dto.ProductResponseDTO;
import com.whitelabel.product.service.impl.ProductServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
class ProductController {

    private static final String PRODUCT_EXAMPLE = """
            {
              "title": "Redmi 18",
              "brand": "Xiaomi",
              "image": "https://example.com/redmi-18.png",
              "gallery": [
                "https://example.com/redmi-18-front.png",
                "https://example.com/redmi-18-back.png"
              ],
              "description": "Lorem Ipsum Dolor Sit",
              "status": "IN",
              "price": {
                "originalPrice": 1000.00,
                "discountPrice": 500.00,
                "discountPercentage": 50
              },
              "categoryId": "9c858901-8a57-4791-81fe-4c455b099bc9"
            }
            """;

    private final ProductServiceImpl productService;

    @Operation(
            summary = "Create a product",
            description = "categoryId must point to an existing category or subcategory (returns 404 otherwise)."
    )
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "categoryId does not match any category")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO create(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product payload", required = true,
                    content = @Content(schema = @Schema(implementation = ProductRequestDTO.class),
                            examples = @ExampleObject(name = "New product", value = PRODUCT_EXAMPLE)))
            @RequestBody ProductRequestDTO productRequestDTO) {
        return productService.create(productRequestDTO);
    }

    @Operation(summary = "List all products")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponseDTO> findAll() {
        return productService.findAll();
    }

    @Operation(summary = "Get a product by id")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/{id}")
    public ProductResponseDTO findById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @Operation(
            summary = "Update a product",
            description = "Replaces every field, including categoryId — send the current categoryId back if it shouldn't change."
    )
    @ApiResponse(responseCode = "200", description = "Product updated")
    @ApiResponse(responseCode = "404", description = "Product or categoryId not found")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponseDTO update(@PathVariable UUID id, @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        return productService.update(id, productRequestDTO);
    }

    @Operation(summary = "Delete a product")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}

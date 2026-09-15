package com.whitelabel.product.category.controller;

import com.whitelabel.product.category.dto.CategoryRequestDTO;
import com.whitelabel.product.category.dto.CategoryResponseDTO;
import com.whitelabel.product.category.service.impl.CategoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Categories",
        description = "Categories form a tree: each category can hold nested subcategories to any depth. "
                + "A create sends the whole tree at once; an update reconciles a subtree against what's already stored."
)
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
class CategoryController {

    private static final String TREE_EXAMPLE = """
            {
              "title": "Acessorios",
              "subcategories": [
                {
                  "title": "Acessorios para Celular",
                  "subcategories": [
                    {
                      "title": "Capas e Cases",
                      "subcategories": [
                        { "title": "Capas de Silicone", "subcategories": [] },
                        { "title": "Capas de Couro", "subcategories": [] }
                      ]
                    },
                    { "title": "Peliculas de Protecao", "subcategories": [] }
                  ]
                }
              ]
            }
            """;

    private static final String UPDATE_EXAMPLE = """
            {
              "title": "Acessorios para Celular",
              "subcategories": [
                {
                  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "title": "Capas e Cases",
                  "subcategories": [
                    { "id": "9c858901-8a57-4791-81fe-4c455b099bc9", "title": "Capas de Silicone", "subcategories": [] }
                  ]
                },
                { "title": "Suportes Veiculares", "subcategories": [] }
              ]
            }
            """;

    private final CategoryServiceImpl categoryService;

    @Operation(
            summary = "Create a category tree",
            description = "Creates a category and, recursively, every subcategory nested under it in the request body. "
                    + "Any `id` sent in the body is ignored — every node is always persisted as new."
    )
    @ApiResponse(responseCode = "201", description = "Tree created")
    @ApiResponse(responseCode = "400", description = "Validation error (e.g. missing title)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO create(
            @Valid @RequestBody(description = "Root category with nested subcategories", required = true,
                    content = @Content(schema = @Schema(implementation = CategoryRequestDTO.class),
                            examples = @ExampleObject(name = "Nested tree", value = TREE_EXAMPLE)))
            @org.springframework.web.bind.annotation.RequestBody CategoryRequestDTO categoryRequestDTO) {
        return categoryService.create(categoryRequestDTO);
    }

    @Operation(
            summary = "List root categories",
            description = "Returns every top-level category (no parent), each with its full subcategory tree nested inside."
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryResponseDTO> findAll() {
        return categoryService.findAll();
    }

    @Operation(
            summary = "Get a category by id",
            description = "Returns the category and its direct/nested subcategories. Works for a root category or any subcategory."
    )
    @ApiResponse(responseCode = "404", description = "Category not found")
    @GetMapping("/{id}")
    public CategoryResponseDTO findById(@PathVariable UUID id) {
        return categoryService.findById(id);
    }

    @Operation(
            summary = "Update a category and reconcile its subtree",
            description = """
                    Renames the category identified by the path `id` and replaces its subcategories using \
                    the `subcategories` array in the body:

                    - A subcategory sent with an existing `id` is matched to the current record and updated in place \
                    (its own `id`, and everything below it, is reconciled the same way, recursively) — safe to use even \
                    when a `Product` already points at that subcategory's `categoryId`.
                    - A subcategory sent without an `id` (or with an unknown one) is created as new.
                    - Any subcategory currently stored under this category but missing from the request body is deleted. \
                    Deleting a subcategory that's still referenced by a `Product.categoryId` returns 409 Conflict — \
                    move or delete those products first.

                    The example below renames "Acessórios" to "Acessórios para Celular", keeps the existing \
                    "Capas e Cases" > "Capas de Silicone" branch (by id), drops "Películas de Proteção" (omitted), \
                    and adds a brand new "Suportes Veiculares" subcategory.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Tree updated")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "409", description = "A removed subcategory is still referenced by a product")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponseDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody(description = "Category title plus the reconciled subcategories tree", required = true,
                    content = @Content(schema = @Schema(implementation = CategoryRequestDTO.class),
                            examples = @ExampleObject(name = "Rename + keep/add/remove subcategories", value = UPDATE_EXAMPLE)))
            @org.springframework.web.bind.annotation.RequestBody CategoryRequestDTO categoryRequestDTO) {
        return categoryService.update(id, categoryRequestDTO);
    }

    @Operation(
            summary = "Delete a category",
            description = "Deletes the category and cascades to every subcategory beneath it. "
                    + "Fails with 409 Conflict if the category or any of its subcategories is still referenced by a Product."
    )
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "409", description = "Category (or a subcategory) is still referenced by a product")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        categoryService.delete(id);
    }
}

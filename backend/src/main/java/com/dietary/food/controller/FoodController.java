package com.dietary.food.controller;

import com.dietary.common.dto.ApiResponse;
import com.dietary.common.security.CurrentUser;
import com.dietary.common.security.UserPrincipal;
import com.dietary.food.controller.dto.FoodDTO;
import com.dietary.food.controller.dto.FoodRequest;
import com.dietary.food.domain.FoodSource;
import com.dietary.food.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@Tag(name = "Foods", description = "Food database management for dietitians")
@SecurityRequirement(name = "bearerAuth")
public class FoodController {

    private final FoodService foodService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Search foods", description = "Search foods by name, category, or source. Returns system foods and dietitian's custom foods.")
    public ResponseEntity<ApiResponse<List<FoodDTO>>> searchFoods(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) FoodSource source,
            @CurrentUser UserPrincipal currentUser) {
        List<FoodDTO> foods = foodService.searchFoods(currentUser.getId(), query, category, source);
        return ResponseEntity.ok(ApiResponse.success(foods));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get food categories", description = "Retrieves all available food categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = foodService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Create custom food", description = "Creates a new custom food entry for the dietitian")
    public ResponseEntity<ApiResponse<FoodDTO>> createFood(
            @Valid @RequestBody FoodRequest request,
            @CurrentUser UserPrincipal currentUser) {
        FoodDTO food = foodService.createFood(request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Food created successfully", food));
    }
}

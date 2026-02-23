package com.dietary.mealplan.controller;

import com.dietary.common.dto.ApiResponse;
import com.dietary.common.security.CurrentUser;
import com.dietary.common.security.UserPrincipal;
import com.dietary.mealplan.controller.dto.MealPlanDTO;
import com.dietary.mealplan.controller.dto.MealPlanRequest;
import com.dietary.mealplan.service.MealPlanService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
@Tag(name = "Meal Plans", description = "Meal plan management with templates and client assignments")
@SecurityRequirement(name = "bearerAuth")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @GetMapping("/templates")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get meal plan templates", description = "Retrieves all meal plan templates created by the dietitian")
    public ResponseEntity<ApiResponse<List<MealPlanDTO>>> getTemplates(
            @CurrentUser UserPrincipal currentUser) {
        List<MealPlanDTO> templates = mealPlanService.getTemplates(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get meal plan by ID", description = "Retrieves a specific meal plan with all meals, options, and items")
    public ResponseEntity<ApiResponse<MealPlanDTO>> getMealPlanById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal currentUser) {
        MealPlanDTO plan = mealPlanService.getMealPlanById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @PostMapping
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Create meal plan", description = "Creates a new meal plan template or client-assigned plan")
    public ResponseEntity<ApiResponse<MealPlanDTO>> createMealPlan(
            @Valid @RequestBody MealPlanRequest request,
            @CurrentUser UserPrincipal currentUser) {
        MealPlanDTO plan = mealPlanService.createMealPlan(request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Meal plan created successfully", plan));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Update meal plan", description = "Updates an existing meal plan with new meals and options")
    public ResponseEntity<ApiResponse<MealPlanDTO>> updateMealPlan(
            @PathVariable UUID id,
            @Valid @RequestBody MealPlanRequest request,
            @CurrentUser UserPrincipal currentUser) {
        MealPlanDTO plan = mealPlanService.updateMealPlan(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Meal plan updated successfully", plan));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Activate meal plan", description = "Activates a meal plan for a client, deactivating any previous active plans")
    public ResponseEntity<ApiResponse<MealPlanDTO>> activateMealPlan(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal currentUser) {
        MealPlanDTO plan = mealPlanService.activateMealPlan(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Meal plan activated successfully", plan));
    }
}

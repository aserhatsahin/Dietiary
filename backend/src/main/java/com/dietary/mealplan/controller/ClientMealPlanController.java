package com.dietary.mealplan.controller;

import com.dietary.common.dto.ApiResponse;
import com.dietary.common.security.CurrentUser;
import com.dietary.common.security.UserPrincipal;
import com.dietary.mealplan.controller.dto.MealPlanDTO;
import com.dietary.mealplan.service.MealPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients/{clientId}/meal-plans")
@RequiredArgsConstructor
@Tag(name = "Client Meal Plans", description = "Client-specific meal plan endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ClientMealPlanController {

    private final MealPlanService mealPlanService;

    @GetMapping
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "List a client's meal plans", description = "All plans assigned to the client, newest first")
    public ResponseEntity<ApiResponse<List<MealPlanDTO>>> getClientMealPlans(
            @PathVariable UUID clientId,
            @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(mealPlanService.getPlansForClient(clientId, currentUser.getId())));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get active meal plan", description = "Retrieves the currently active meal plan for a client")
    public ResponseEntity<ApiResponse<MealPlanDTO>> getActiveMealPlan(
            @PathVariable UUID clientId,
            @CurrentUser UserPrincipal currentUser) {
        MealPlanDTO plan = mealPlanService.getActivePlanForClient(clientId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(plan));
    }
}

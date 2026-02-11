package com.dietary.goal.controller;

import com.dietary.common.dto.ApiResponse;
import com.dietary.common.security.CurrentUser;
import com.dietary.common.security.UserPrincipal;
import com.dietary.goal.controller.dto.GoalDTO;
import com.dietary.goal.controller.dto.GoalRequest;
import com.dietary.goal.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clients/{clientId}/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Client goal management with BMR/TDEE calculations")
@SecurityRequirement(name = "bearerAuth")
public class GoalController {

    private final GoalService goalService;

    @GetMapping("/current")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get current goal", description = "Retrieves the active goal for a client with calculated BMR, TDEE, and calorie targets")
    public ResponseEntity<ApiResponse<GoalDTO>> getCurrentGoal(
            @PathVariable UUID clientId,
            @CurrentUser UserPrincipal currentUser) {
        GoalDTO goal = goalService.getCurrentGoal(clientId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    @PostMapping
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Create or replace goal", description = "Creates a new goal for a client, deactivating any existing active goal. Calculates BMR, TDEE, and daily calorie target automatically.")
    public ResponseEntity<ApiResponse<GoalDTO>> createOrReplaceGoal(
            @PathVariable UUID clientId,
            @Valid @RequestBody GoalRequest request,
            @CurrentUser UserPrincipal currentUser) {
        GoalDTO goal = goalService.createOrReplaceGoal(clientId, request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created successfully", goal));
    }
}

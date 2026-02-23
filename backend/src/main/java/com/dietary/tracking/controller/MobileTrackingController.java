package com.dietary.tracking.controller;

import com.dietary.common.dto.ApiResponse;
import com.dietary.common.security.CurrentUser;
import com.dietary.common.security.UserPrincipal;
import com.dietary.tracking.controller.dto.*;
import com.dietary.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/mobile/tracking")
@RequiredArgsConstructor
@Tag(name = "Mobile Tracking", description = "Client-facing mobile app tracking endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MobileTrackingController {

    private final TrackingService trackingService;

    @GetMapping("/daily")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get daily plan", description = "Retrieves the meal plan for a specific date with tracking status")
    public ResponseEntity<ApiResponse<DailyPlanDTO>> getDailyPlan(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @CurrentUser UserPrincipal currentUser) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        DailyPlanDTO plan = trackingService.getDailyPlan(currentUser.getClientId(), targetDate);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @PostMapping("/meals")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Track meal", description = "Records meal completion with selected option")
    public ResponseEntity<ApiResponse<DailyMealDTO>> trackMeal(
            @Valid @RequestBody TrackMealRequest request,
            @CurrentUser UserPrincipal currentUser) {
        DailyMealDTO result = trackingService.trackMeal(currentUser.getClientId(), request);
        return ResponseEntity.ok(ApiResponse.success("Meal tracked successfully", result));
    }

    @PostMapping("/water")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Track water", description = "Records water intake and returns total for the day")
    public ResponseEntity<ApiResponse<Integer>> trackWater(
            @Valid @RequestBody TrackWaterRequest request,
            @CurrentUser UserPrincipal currentUser) {
        Integer totalWater = trackingService.trackWater(currentUser.getClientId(), request);
        return ResponseEntity.ok(ApiResponse.success("Water tracked successfully", totalWater));
    }
}

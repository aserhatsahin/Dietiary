package com.dietary.client.controller;

import com.dietary.client.controller.dto.ClientDTO;
import com.dietary.client.service.ClientContextService;
import com.dietary.client.service.ClientContextService.ClientContext;
import com.dietary.client.service.ClientService;
import com.dietary.common.dto.ApiResponse;
import com.dietary.common.security.CurrentUser;
import com.dietary.common.security.UserPrincipal;
import com.dietary.goal.controller.dto.GoalDTO;
import com.dietary.goal.service.GoalService;
import com.dietary.measurement.controller.dto.MeasurementDTO;
import com.dietary.measurement.service.MeasurementService;
import com.dietary.tracking.controller.dto.ProgressDTO;
import com.dietary.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Read-only endpoints for a logged-in client: their own profile, measurements, goal and progress.
 * Each call resolves the caller's client record and reuses the dietitian-scoped services with it,
 * so a client can only ever read their own data.
 */
@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "Mobile Client", description = "Client-facing endpoints for the client's own data")
@SecurityRequirement(name = "bearerAuth")
public class MobileClientController {

    static final int DEFAULT_PROGRESS_DAYS = 30;

    private final ClientContextService clientContextService;
    private final ClientService clientService;
    private final MeasurementService measurementService;
    private final GoalService goalService;
    private final TrackingService trackingService;

    @GetMapping("/me")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<ClientDTO>> getMyProfile(@CurrentUser UserPrincipal currentUser) {
        ClientContext ctx = clientContextService.require(currentUser);
        return ResponseEntity.ok(ApiResponse.success(clientService.getClientById(ctx.clientId(), ctx.dietitianId())));
    }

    @GetMapping("/measurements")
    @Operation(summary = "Get my measurements", description = "Newest first")
    public ResponseEntity<ApiResponse<List<MeasurementDTO>>> getMyMeasurements(@CurrentUser UserPrincipal currentUser) {
        ClientContext ctx = clientContextService.require(currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                measurementService.getAllMeasurements(ctx.clientId(), ctx.dietitianId())));
    }

    @GetMapping("/measurements/latest")
    @Operation(summary = "Get my latest measurement")
    public ResponseEntity<ApiResponse<MeasurementDTO>> getMyLatestMeasurement(@CurrentUser UserPrincipal currentUser) {
        ClientContext ctx = clientContextService.require(currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                measurementService.getLatestMeasurement(ctx.clientId(), ctx.dietitianId())));
    }

    @GetMapping("/goal")
    @Operation(summary = "Get my current goal")
    public ResponseEntity<ApiResponse<GoalDTO>> getMyGoal(@CurrentUser UserPrincipal currentUser) {
        ClientContext ctx = clientContextService.require(currentUser);
        return ResponseEntity.ok(ApiResponse.success(goalService.getCurrentGoal(ctx.clientId(), ctx.dietitianId())));
    }

    @GetMapping("/progress")
    @Operation(summary = "Get my progress",
            description = "Weight trend, compliance and averages. Defaults to the last 30 days.")
    public ResponseEntity<ApiResponse<ProgressDTO>> getMyProgress(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @CurrentUser UserPrincipal currentUser) {
        ClientContext ctx = clientContextService.require(currentUser);
        LocalDate to = toDate != null ? toDate : LocalDate.now();
        LocalDate from = fromDate != null ? fromDate : to.minusDays(DEFAULT_PROGRESS_DAYS - 1);
        return ResponseEntity.ok(ApiResponse.success(
                trackingService.getProgress(ctx.clientId(), ctx.dietitianId(), from, to)));
    }
}

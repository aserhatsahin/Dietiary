package com.dietary.measurement.controller;

import com.dietary.common.dto.ApiResponse;
import com.dietary.common.security.CurrentUser;
import com.dietary.common.security.UserPrincipal;
import com.dietary.measurement.controller.dto.MeasurementDTO;
import com.dietary.measurement.controller.dto.MeasurementRequest;
import com.dietary.measurement.service.MeasurementService;
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
@RequestMapping("/api/clients/{clientId}/measurements")
@RequiredArgsConstructor
@Tag(name = "Measurements", description = "Client measurement endpoints for dietitians")
@SecurityRequirement(name = "bearerAuth")
public class MeasurementController {

    private final MeasurementService measurementService;

    @GetMapping
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get all measurements", description = "Retrieves all measurements for a client with delta comparisons")
    public ResponseEntity<ApiResponse<List<MeasurementDTO>>> getAllMeasurements(
            @PathVariable UUID clientId,
            @CurrentUser UserPrincipal currentUser) {
        List<MeasurementDTO> measurements = measurementService.getAllMeasurements(clientId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(measurements));
    }

    @GetMapping("/latest")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get latest measurement", description = "Retrieves the most recent measurement for a client with delta from previous")
    public ResponseEntity<ApiResponse<MeasurementDTO>> getLatestMeasurement(
            @PathVariable UUID clientId,
            @CurrentUser UserPrincipal currentUser) {
        MeasurementDTO measurement = measurementService.getLatestMeasurement(clientId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(measurement));
    }

    @PostMapping
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Create a measurement", description = "Records a new measurement for a client with automatic BMI calculation")
    public ResponseEntity<ApiResponse<MeasurementDTO>> createMeasurement(
            @PathVariable UUID clientId,
            @Valid @RequestBody MeasurementRequest request,
            @CurrentUser UserPrincipal currentUser) {
        MeasurementDTO measurement = measurementService.createMeasurement(clientId, request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Measurement recorded successfully", measurement));
    }
}

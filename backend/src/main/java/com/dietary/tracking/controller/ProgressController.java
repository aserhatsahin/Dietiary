package com.dietary.tracking.controller;

import com.dietary.common.dto.ApiResponse;
import com.dietary.common.security.CurrentUser;
import com.dietary.common.security.UserPrincipal;
import com.dietary.tracking.controller.dto.ProgressDTO;
import com.dietary.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients/{clientId}/progress")
@RequiredArgsConstructor
@Tag(name = "Client Progress", description = "Dietitian-facing client progress and analytics")
@SecurityRequirement(name = "bearerAuth")
public class ProgressController {

    private final TrackingService trackingService;

    @GetMapping
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get client progress", description = "Retrieves client progress including weight trend, compliance, and averages")
    public ResponseEntity<ApiResponse<ProgressDTO>> getProgress(
            @PathVariable UUID clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @CurrentUser UserPrincipal currentUser) {
        ProgressDTO progress = trackingService.getProgress(clientId, currentUser.getId(), fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
}

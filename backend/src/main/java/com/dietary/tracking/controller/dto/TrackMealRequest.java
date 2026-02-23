package com.dietary.tracking.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackMealRequest {

    @NotNull(message = "Meal ID is required")
    private UUID mealId;

    @NotNull(message = "Selected option ID is required")
    private UUID selectedOptionId;

    private LocalDate trackingDate; // Defaults to today if not provided

    private String notes;
}

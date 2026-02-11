package com.dietary.measurement.controller.dto;

import com.dietary.measurement.domain.EntryMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementRequest {

    @NotNull(message = "Measurement date is required")
    @Past(message = "Measurement date must be in the past or today")
    private LocalDate measurementDate;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private BigDecimal weightKg;

    @Positive(message = "Body fat percentage must be positive")
    private BigDecimal bodyFatPercentage;

    @Positive(message = "Muscle mass must be positive")
    private BigDecimal muscleMassKg;

    @Positive(message = "Water percentage must be positive")
    private BigDecimal waterPercentage;

    @Positive(message = "Bone mass must be positive")
    private BigDecimal boneMassKg;

    @Positive(message = "Visceral fat must be positive")
    private Integer visceralFat;

    @Positive(message = "Basal metabolic rate must be positive")
    private Integer basalMetabolicRate;

    @Positive(message = "Metabolic age must be positive")
    private Integer metabolicAge;

    private String notes;

    @Builder.Default
    private EntryMethod entryMethod = EntryMethod.MANUAL;
}

package com.dietary.measurement.controller.dto;

import com.dietary.measurement.domain.EntryMethod;
import com.dietary.measurement.domain.Measurement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementDTO {

    private UUID id;
    private UUID clientId;
    private LocalDate measurementDate;
    private BigDecimal weightKg;
    private BigDecimal bodyFatPercentage;
    private BigDecimal muscleMassKg;
    private BigDecimal waterPercentage;
    private BigDecimal boneMassKg;
    private Integer visceralFat;
    private Integer basalMetabolicRate;
    private Integer metabolicAge;
    private BigDecimal bmi;
    private String notes;
    private EntryMethod entryMethod;
    private Instant createdAt;

    // Delta fields compared to previous measurement
    private BigDecimal weightDelta;
    private BigDecimal bodyFatDelta;
    private BigDecimal muscleMassDelta;
    private BigDecimal bmiDelta;

    public static MeasurementDTO fromEntity(Measurement measurement) {
        return MeasurementDTO.builder()
                .id(measurement.getId())
                .clientId(measurement.getClient().getId())
                .measurementDate(measurement.getMeasurementDate())
                .weightKg(measurement.getWeightKg())
                .bodyFatPercentage(measurement.getBodyFatPercentage())
                .muscleMassKg(measurement.getMuscleMassKg())
                .waterPercentage(measurement.getWaterPercentage())
                .boneMassKg(measurement.getBoneMassKg())
                .visceralFat(measurement.getVisceralFat())
                .basalMetabolicRate(measurement.getBasalMetabolicRate())
                .metabolicAge(measurement.getMetabolicAge())
                .bmi(measurement.getBmi())
                .notes(measurement.getNotes())
                .createdAt(measurement.getCreatedAt())
                .build();
    }

    public static MeasurementDTO fromEntityWithDelta(Measurement current, Measurement previous) {
        MeasurementDTO dto = fromEntity(current);
        if (previous != null) {
            if (current.getWeightKg() != null && previous.getWeightKg() != null) {
                dto.setWeightDelta(current.getWeightKg().subtract(previous.getWeightKg()));
            }
            if (current.getBodyFatPercentage() != null && previous.getBodyFatPercentage() != null) {
                dto.setBodyFatDelta(current.getBodyFatPercentage().subtract(previous.getBodyFatPercentage()));
            }
            if (current.getMuscleMassKg() != null && previous.getMuscleMassKg() != null) {
                dto.setMuscleMassDelta(current.getMuscleMassKg().subtract(previous.getMuscleMassKg()));
            }
            if (current.getBmi() != null && previous.getBmi() != null) {
                dto.setBmiDelta(current.getBmi().subtract(previous.getBmi()));
            }
        }
        return dto;
    }
}

package com.dietary.tracking.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDTO {

    private LocalDate fromDate;
    private LocalDate toDate;

    // Weight trend
    private BigDecimal startWeight;
    private BigDecimal currentWeight;
    private BigDecimal weightChange;
    private List<WeightEntry> weightHistory;

    // Compliance summary
    private Integer totalDays;
    private Integer daysTracked;
    private Double compliancePercentage;
    private Integer totalMealsExpected;
    private Integer mealsCompleted;
    private Double mealCompliancePercentage;

    // Averages
    private Integer avgDailyCalories;
    private Integer avgDailyWaterMl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeightEntry {
        private LocalDate date;
        private BigDecimal weightKg;
        private BigDecimal bmi;
    }
}

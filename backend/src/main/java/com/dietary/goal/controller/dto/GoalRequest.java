package com.dietary.goal.controller.dto;

import com.dietary.goal.domain.ActivityLevel;
import com.dietary.goal.domain.GoalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {

    @NotNull(message = "Goal type is required")
    private GoalType goalType;

    @Positive(message = "Target weight must be positive")
    private BigDecimal targetWeightKg;

    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @Positive(message = "Weekly weight change must be positive")
    private BigDecimal weeklyWeightChangeKg;

    // Optional macro overrides (if not provided, will be calculated)
    private Integer proteinGrams;
    private Integer carbsGrams;
    private Integer fatGrams;

    private String notes;
}

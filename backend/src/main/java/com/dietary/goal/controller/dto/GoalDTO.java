package com.dietary.goal.controller.dto;

import com.dietary.goal.domain.ActivityLevel;
import com.dietary.goal.domain.Goal;
import com.dietary.goal.domain.GoalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalDTO {

    private UUID id;
    private UUID clientId;
    private GoalType goalType;
    private BigDecimal targetWeightKg;
    private ActivityLevel activityLevel;
    private Integer bmr;
    private Integer tdee;
    private Integer dailyCalorieTarget;
    private Integer proteinGrams;
    private Integer carbsGrams;
    private Integer fatGrams;
    private BigDecimal weeklyWeightChangeKg;
    private Boolean isActive;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    // Calculation details for transparency
    private String bmrFormula;
    private Double activityMultiplier;
    private Integer calorieAdjustment;

    public static GoalDTO fromEntity(Goal goal) {
        return GoalDTO.builder()
                .id(goal.getId())
                .clientId(goal.getClient().getId())
                .goalType(goal.getGoalType())
                .targetWeightKg(goal.getTargetWeightKg())
                .activityLevel(goal.getActivityLevel())
                .bmr(goal.getBmr())
                .tdee(goal.getTdee())
                .dailyCalorieTarget(goal.getDailyCalorieTarget())
                .proteinGrams(goal.getProteinGrams())
                .carbsGrams(goal.getCarbsGrams())
                .fatGrams(goal.getFatGrams())
                .weeklyWeightChangeKg(goal.getWeeklyWeightChangeKg())
                .isActive(goal.getIsActive())
                .notes(goal.getNotes())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}

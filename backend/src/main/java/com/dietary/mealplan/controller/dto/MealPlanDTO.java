package com.dietary.mealplan.controller.dto;

import com.dietary.mealplan.domain.MealPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanDTO {

    private UUID id;
    private UUID clientId;
    private String clientName;
    private String name;
    private String description;
    private Integer dailyCalories;
    private Integer totalMeals;
    private Boolean isActive;
    private Boolean isTemplate;
    private List<MealDTO> meals;
    private Instant createdAt;
    private Instant updatedAt;

    public static MealPlanDTO fromEntity(MealPlan plan) {
        List<MealDTO> mealDTOs = plan.getMeals().stream()
                .map(MealDTO::fromEntity)
                .collect(Collectors.toList());

        // Calculate total daily calories from all meals
        int totalCals = mealDTOs.stream()
                .flatMap(m -> m.getOptions().stream())
                .mapToInt(o -> o.getTotalCalories() != null ? o.getTotalCalories() : 0)
                .sum();

        return MealPlanDTO.builder()
                .id(plan.getId())
                .clientId(plan.getClient() != null ? plan.getClient().getId() : null)
                .clientName(plan.getClient() != null ? plan.getClient().getFullName() : null)
                .name(plan.getName())
                .description(plan.getDescription())
                .dailyCalories(plan.getDailyCalories() != null ? plan.getDailyCalories() : totalCals)
                .totalMeals(plan.getMeals().size())
                .isActive(plan.getIsActive())
                .isTemplate(plan.getClient() == null)
                .meals(mealDTOs)
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    public static MealPlanDTO fromEntitySummary(MealPlan plan) {
        return MealPlanDTO.builder()
                .id(plan.getId())
                .clientId(plan.getClient() != null ? plan.getClient().getId() : null)
                .clientName(plan.getClient() != null ? plan.getClient().getFullName() : null)
                .name(plan.getName())
                .description(plan.getDescription())
                .dailyCalories(plan.getDailyCalories())
                .totalMeals(plan.getMeals().size())
                .isActive(plan.getIsActive())
                .isTemplate(plan.getClient() == null)
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}

package com.dietary.tracking.controller.dto;

import com.dietary.mealplan.controller.dto.MealDTO;
import com.dietary.mealplan.controller.dto.MealPlanDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanDTO {

    private LocalDate date;
    private UUID mealPlanId;
    private String mealPlanName;
    private Integer dailyCalorieTarget;
    private List<DailyMealDTO> meals;
    private Integer totalWaterMl;
    private Integer caloriesConsumed;
    private Integer mealsCompleted;
    private Integer totalMeals;
}

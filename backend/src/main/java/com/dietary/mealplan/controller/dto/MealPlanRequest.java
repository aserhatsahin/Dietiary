package com.dietary.mealplan.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanRequest {

    @NotBlank(message = "Meal plan name is required")
    private String name;

    private String description;

    private UUID clientId; // Required for client plans, null for templates

    private Integer dailyCalories;

    private Boolean isTemplate; // If true, creates a template; if false, assigns to client

    @Valid
    private List<MealRequest> meals;
}

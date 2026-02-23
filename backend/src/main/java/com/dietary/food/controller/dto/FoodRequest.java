package com.dietary.food.controller.dto;

import com.dietary.food.domain.FoodSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRequest {

    @NotBlank(message = "Food name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    private String name;

    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;

    @NotBlank(message = "Serving size is required")
    private String servingSize;

    @NotBlank(message = "Serving unit is required")
    private String servingUnit;

    @NotNull(message = "Calories per serving is required")
    @Positive(message = "Calories must be positive")
    private Integer caloriesPerServing;

    @Positive(message = "Protein must be positive")
    private BigDecimal proteinGrams;

    @Positive(message = "Carbs must be positive")
    private BigDecimal carbsGrams;

    @Positive(message = "Fat must be positive")
    private BigDecimal fatGrams;

    @Positive(message = "Fiber must be positive")
    private BigDecimal fiberGrams;

    @Positive(message = "Sugar must be positive")
    private BigDecimal sugarGrams;

    @Positive(message = "Sodium must be positive")
    private BigDecimal sodiumMg;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}

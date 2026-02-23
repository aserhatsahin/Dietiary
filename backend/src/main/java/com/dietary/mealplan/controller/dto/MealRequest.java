package com.dietary.mealplan.controller.dto;

import com.dietary.mealplan.domain.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealRequest {

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @NotBlank(message = "Meal name is required")
    private String name;

    private Integer displayOrder;

    @Valid
    private List<MealOptionRequest> options;
}

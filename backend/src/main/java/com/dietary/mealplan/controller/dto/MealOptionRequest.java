package com.dietary.mealplan.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealOptionRequest {

    @NotBlank(message = "Option name is required")
    private String name;

    private String description;

    private Integer displayOrder;

    @Valid
    private List<MealOptionItemRequest> items;
}

package com.dietary.mealplan.controller.dto;

import com.dietary.mealplan.domain.Meal;
import com.dietary.mealplan.domain.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealDTO {

    private UUID id;
    private MealType mealType;
    private String name;
    private Integer displayOrder;
    private List<MealOptionDTO> options;

    public static MealDTO fromEntity(Meal meal) {
        List<MealOptionDTO> optionDTOs = meal.getOptions().stream()
                .map(MealOptionDTO::fromEntity)
                .collect(Collectors.toList());

        return MealDTO.builder()
                .id(meal.getId())
                .mealType(meal.getMealType())
                .name(meal.getName())
                .displayOrder(meal.getDisplayOrder())
                .options(optionDTOs)
                .build();
    }
}

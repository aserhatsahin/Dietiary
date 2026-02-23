package com.dietary.mealplan.controller.dto;

import com.dietary.mealplan.domain.MealOption;
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
public class MealOptionDTO {

    private UUID id;
    private String name;
    private String description;
    private Integer totalCalories;
    private Integer totalProtein;
    private Integer totalCarbs;
    private Integer totalFat;
    private Integer displayOrder;
    private List<MealOptionItemDTO> items;

    public static MealOptionDTO fromEntity(MealOption option) {
        List<MealOptionItemDTO> itemDTOs = option.getItems().stream()
                .map(MealOptionItemDTO::fromEntity)
                .collect(Collectors.toList());

        // Calculate totals from items
        int totalCals = option.getItems().stream()
                .mapToInt(i -> i.getCalories() != null ? i.getCalories() : 0)
                .sum();

        return MealOptionDTO.builder()
                .id(option.getId())
                .name(option.getName())
                .description(option.getDescription())
                .totalCalories(totalCals > 0 ? totalCals : option.getTotalCalories())
                .displayOrder(option.getDisplayOrder())
                .items(itemDTOs)
                .build();
    }
}

package com.dietary.mealplan.controller.dto;

import com.dietary.food.controller.dto.FoodDTO;
import com.dietary.mealplan.domain.MealOptionItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealOptionItemDTO {

    private UUID id;
    private UUID foodId;
    private String foodName;
    private BigDecimal quantity;
    private String quantityUnit;
    private Integer calories;
    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;
    private Integer displayOrder;
    private String notes;

    public static MealOptionItemDTO fromEntity(MealOptionItem item) {
        return MealOptionItemDTO.builder()
                .id(item.getId())
                .foodId(item.getFood().getId())
                .foodName(item.getFood().getName())
                .quantity(item.getQuantity())
                .quantityUnit(item.getQuantityUnit())
                .calories(item.getCalories())
                .displayOrder(item.getDisplayOrder())
                .notes(item.getNotes())
                .build();
    }
}

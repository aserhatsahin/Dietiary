package com.dietary.food.controller.dto;

import com.dietary.food.domain.Food;
import com.dietary.food.domain.FoodSource;
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
public class FoodDTO {

    private UUID id;
    private String name;
    private String brand;
    private String servingSize;
    private String servingUnit;
    private Integer caloriesPerServing;
    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;
    private BigDecimal fiberGrams;
    private BigDecimal sugarGrams;
    private BigDecimal sodiumMg;
    private String category;
    private String notes;
    private FoodSource source;
    private Boolean active;
    private Instant createdAt;

    public static FoodDTO fromEntity(Food food) {
        return FoodDTO.builder()
                .id(food.getId())
                .name(food.getName())
                .brand(food.getBrand())
                .servingSize(food.getServingSize())
                .servingUnit(food.getServingUnit())
                .caloriesPerServing(food.getCaloriesPerServing())
                .proteinGrams(food.getProteinGrams())
                .carbsGrams(food.getCarbsGrams())
                .fatGrams(food.getFatGrams())
                .fiberGrams(food.getFiberGrams())
                .sugarGrams(food.getSugarGrams())
                .sodiumMg(food.getSodiumMg())
                .category(food.getCategory())
                .notes(food.getNotes())
                .source(food.getSource())
                .active(food.getActive())
                .createdAt(food.getCreatedAt())
                .build();
    }
}

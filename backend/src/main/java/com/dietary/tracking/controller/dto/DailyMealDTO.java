package com.dietary.tracking.controller.dto;

import com.dietary.mealplan.controller.dto.MealOptionDTO;
import com.dietary.mealplan.domain.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMealDTO {

    private UUID mealId;
    private MealType mealType;
    private String name;
    private Integer displayOrder;
    private Boolean isCompleted;
    private UUID selectedOptionId;
    private Instant completedAt;
    private List<MealOptionDTO> options;
}

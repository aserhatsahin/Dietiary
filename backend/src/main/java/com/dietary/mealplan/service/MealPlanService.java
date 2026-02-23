package com.dietary.mealplan.service;

import com.dietary.auth.domain.User;
import com.dietary.auth.repository.UserRepository;
import com.dietary.client.domain.Client;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.exception.BadRequestException;
import com.dietary.common.exception.ResourceNotFoundException;
import com.dietary.food.domain.Food;
import com.dietary.food.repository.FoodRepository;
import com.dietary.mealplan.controller.dto.*;
import com.dietary.mealplan.domain.*;
import com.dietary.mealplan.repository.MealPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;

    @Transactional(readOnly = true)
    public List<MealPlanDTO> getTemplates(UUID dietitianId) {
        return mealPlanRepository.findAllTemplatesByDietitianId(dietitianId).stream()
                .map(MealPlanDTO::fromEntitySummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MealPlanDTO getMealPlanById(UUID planId, UUID dietitianId) {
        MealPlan plan = mealPlanRepository.findByIdAndDietitianId(planId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("MealPlan", "id", planId));
        return MealPlanDTO.fromEntity(plan);
    }

    @Transactional(readOnly = true)
    public MealPlanDTO getActivePlanForClient(UUID clientId, UUID dietitianId) {
        MealPlan plan = mealPlanRepository.findActiveByClientIdAndDietitianId(clientId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("Active meal plan", "clientId", clientId));
        return MealPlanDTO.fromEntity(plan);
    }

    @Transactional
    public MealPlanDTO createMealPlan(MealPlanRequest request, UUID dietitianId) {
        User dietitian = userRepository.findById(dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dietitianId));

        Client client = null;
        if (request.getClientId() != null && (request.getIsTemplate() == null || !request.getIsTemplate())) {
            client = clientRepository.findByIdAndDietitianId(request.getClientId(), dietitianId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));
        }

        MealPlan mealPlan = MealPlan.builder()
                .dietitian(dietitian)
                .client(client)
                .name(request.getName().trim())
                .description(request.getDescription())
                .dailyCalories(request.getDailyCalories())
                .isActive(false)
                .build();

        // Add meals if provided
        if (request.getMeals() != null) {
            for (int i = 0; i < request.getMeals().size(); i++) {
                MealRequest mealRequest = request.getMeals().get(i);
                Meal meal = createMeal(mealRequest, i + 1);
                mealPlan.addMeal(meal);
            }
        }

        mealPlan = mealPlanRepository.save(mealPlan);
        log.info("Created meal plan '{}' for dietitian '{}'", mealPlan.getName(), dietitian.getEmail());

        return MealPlanDTO.fromEntity(mealPlan);
    }

    @Transactional
    public MealPlanDTO updateMealPlan(UUID planId, MealPlanRequest request, UUID dietitianId) {
        MealPlan mealPlan = mealPlanRepository.findByIdAndDietitianId(planId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("MealPlan", "id", planId));

        mealPlan.setName(request.getName().trim());
        mealPlan.setDescription(request.getDescription());
        mealPlan.setDailyCalories(request.getDailyCalories());

        // Clear and recreate meals if provided
        if (request.getMeals() != null) {
            mealPlan.getMeals().clear();
            for (int i = 0; i < request.getMeals().size(); i++) {
                MealRequest mealRequest = request.getMeals().get(i);
                Meal meal = createMeal(mealRequest, i + 1);
                mealPlan.addMeal(meal);
            }
        }

        mealPlan = mealPlanRepository.save(mealPlan);
        log.info("Updated meal plan '{}'", mealPlan.getName());

        return MealPlanDTO.fromEntity(mealPlan);
    }

    @Transactional
    public MealPlanDTO activateMealPlan(UUID planId, UUID dietitianId) {
        MealPlan mealPlan = mealPlanRepository.findByIdAndDietitianId(planId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("MealPlan", "id", planId));

        if (mealPlan.getClient() == null) {
            throw new BadRequestException("Cannot activate a template. Assign it to a client first.");
        }

        // Deactivate any existing active plans for this client
        mealPlanRepository.deactivateAllByClientId(mealPlan.getClient().getId());

        // Activate this plan
        mealPlan.setIsActive(true);
        mealPlan = mealPlanRepository.save(mealPlan);

        log.info("Activated meal plan '{}' for client '{}'",
                mealPlan.getName(), mealPlan.getClient().getFullName());

        return MealPlanDTO.fromEntity(mealPlan);
    }

    private Meal createMeal(MealRequest request, int defaultOrder) {
        Meal meal = Meal.builder()
                .mealType(request.getMealType())
                .name(request.getName().trim())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : defaultOrder)
                .build();

        if (request.getOptions() != null) {
            for (int i = 0; i < request.getOptions().size(); i++) {
                MealOptionRequest optionRequest = request.getOptions().get(i);
                MealOption option = createMealOption(optionRequest, i + 1);
                meal.addOption(option);
            }
        }

        return meal;
    }

    private MealOption createMealOption(MealOptionRequest request, int defaultOrder) {
        MealOption option = MealOption.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : defaultOrder)
                .build();

        int totalCalories = 0;

        if (request.getItems() != null) {
            for (int i = 0; i < request.getItems().size(); i++) {
                MealOptionItemRequest itemRequest = request.getItems().get(i);
                MealOptionItem item = createMealOptionItem(itemRequest, i + 1);
                option.addItem(item);
                if (item.getCalories() != null) {
                    totalCalories += item.getCalories();
                }
            }
        }

        option.setTotalCalories(totalCalories);
        return option;
    }

    private MealOptionItem createMealOptionItem(MealOptionItemRequest request, int defaultOrder) {
        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food", "id", request.getFoodId()));

        // Calculate calories based on quantity
        int calories = calculateCalories(food, request.getQuantity(), request.getQuantityUnit());

        return MealOptionItem.builder()
                .food(food)
                .quantity(request.getQuantity())
                .quantityUnit(request.getQuantityUnit())
                .calories(calories)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : defaultOrder)
                .notes(request.getNotes())
                .build();
    }

    private int calculateCalories(Food food, BigDecimal quantity, String unit) {
        // Simple calculation: assume serving size is in same unit for now
        // Calories = (quantity / serving_size) * calories_per_serving
        try {
            BigDecimal servingSize = new BigDecimal(food.getServingSize());
            BigDecimal ratio = quantity.divide(servingSize, 4, RoundingMode.HALF_UP);
            return ratio.multiply(BigDecimal.valueOf(food.getCaloriesPerServing()))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        } catch (NumberFormatException e) {
            // If serving size is not a number (e.g., "1 medium"), use calories directly
            return food.getCaloriesPerServing();
        }
    }
}

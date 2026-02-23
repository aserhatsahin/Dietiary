package com.dietary.food.service;

import com.dietary.auth.domain.User;
import com.dietary.auth.repository.UserRepository;
import com.dietary.common.exception.ResourceNotFoundException;
import com.dietary.food.controller.dto.FoodDTO;
import com.dietary.food.controller.dto.FoodRequest;
import com.dietary.food.domain.Food;
import com.dietary.food.domain.FoodSource;
import com.dietary.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FoodDTO> searchFoods(UUID dietitianId, String query, String category, FoodSource source) {
        return foodRepository.searchFoods(dietitianId, query, category, source).stream()
                .map(FoodDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return foodRepository.findAllCategories();
    }

    @Transactional
    public FoodDTO createFood(FoodRequest request, UUID dietitianId) {
        User dietitian = userRepository.findById(dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dietitianId));

        // CUSTOM foods always require a dietitian
        Food food = Food.builder()
                .dietitian(dietitian)
                .source(FoodSource.CUSTOM)
                .name(request.getName().trim())
                .brand(request.getBrand())
                .servingSize(request.getServingSize())
                .servingUnit(request.getServingUnit())
                .caloriesPerServing(request.getCaloriesPerServing())
                .proteinGrams(request.getProteinGrams())
                .carbsGrams(request.getCarbsGrams())
                .fatGrams(request.getFatGrams())
                .fiberGrams(request.getFiberGrams())
                .sugarGrams(request.getSugarGrams())
                .sodiumMg(request.getSodiumMg())
                .category(request.getCategory())
                .notes(request.getNotes())
                .active(true)
                .build();

        food = foodRepository.save(food);
        log.info("Created custom food '{}' for dietitian '{}'", food.getName(), dietitian.getEmail());

        return FoodDTO.fromEntity(food);
    }
}

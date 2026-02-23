package com.dietary.tracking.service;

import com.dietary.client.domain.Client;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.exception.BadRequestException;
import com.dietary.common.exception.ResourceNotFoundException;
import com.dietary.mealplan.controller.dto.MealOptionDTO;
import com.dietary.mealplan.domain.Meal;
import com.dietary.mealplan.domain.MealOption;
import com.dietary.mealplan.domain.MealPlan;
import com.dietary.mealplan.repository.MealPlanRepository;
import com.dietary.measurement.domain.Measurement;
import com.dietary.measurement.repository.MeasurementRepository;
import com.dietary.tracking.controller.dto.*;
import com.dietary.tracking.domain.DailyTracking;
import com.dietary.tracking.domain.WaterTracking;
import com.dietary.tracking.repository.DailyTrackingRepository;
import com.dietary.tracking.repository.WaterTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final DailyTrackingRepository dailyTrackingRepository;
    private final WaterTrackingRepository waterTrackingRepository;
    private final MealPlanRepository mealPlanRepository;
    private final MeasurementRepository measurementRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public DailyPlanDTO getDailyPlan(UUID clientId, LocalDate date) {
        MealPlan activePlan = mealPlanRepository.findActiveByClientId(clientId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No active meal plan for client", "clientId", clientId));

        List<DailyTracking> trackings = dailyTrackingRepository.findByClientIdAndDate(clientId, date);
        Map<UUID, DailyTracking> trackingByMealId = trackings.stream()
                .collect(Collectors.toMap(t -> t.getMeal().getId(), t -> t, (a, b) -> a));

        int totalWater = waterTrackingRepository.sumByClientIdAndDate(clientId, date);

        List<DailyMealDTO> mealDTOs = activePlan.getMeals().stream()
                .sorted(Comparator.comparing(Meal::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(meal -> {
                    DailyTracking tracking = trackingByMealId.get(meal.getId());
                    return DailyMealDTO.builder()
                            .mealId(meal.getId())
                            .mealType(meal.getMealType())
                            .name(meal.getName())
                            .displayOrder(meal.getDisplayOrder())
                            .isCompleted(tracking != null && tracking.getIsCompleted())
                            .selectedOptionId(tracking != null ? tracking.getSelectedOption().getId() : null)
                            .completedAt(tracking != null ? tracking.getCompletedAt() : null)
                            .options(meal.getOptions().stream()
                                    .map(MealOptionDTO::fromEntity)
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());

        int mealsCompleted = (int) mealDTOs.stream().filter(DailyMealDTO::getIsCompleted).count();
        int caloriesConsumed = trackings.stream()
                .filter(DailyTracking::getIsCompleted)
                .mapToInt(
                        t -> t.getSelectedOption().getTotalCalories() != null ? t.getSelectedOption().getTotalCalories()
                                : 0)
                .sum();

        return DailyPlanDTO.builder()
                .date(date)
                .mealPlanId(activePlan.getId())
                .mealPlanName(activePlan.getName())
                .dailyCalorieTarget(activePlan.getDailyCalories())
                .meals(mealDTOs)
                .totalWaterMl(totalWater)
                .caloriesConsumed(caloriesConsumed)
                .mealsCompleted(mealsCompleted)
                .totalMeals(mealDTOs.size())
                .build();
    }

    @Transactional
    public DailyMealDTO trackMeal(UUID clientId, TrackMealRequest request) {
        LocalDate date = request.getTrackingDate() != null ? request.getTrackingDate() : LocalDate.now();

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        MealPlan activePlan = mealPlanRepository.findActiveByClientId(clientId)
                .orElseThrow(() -> new BadRequestException("No active meal plan for client"));

        Meal meal = activePlan.getMeals().stream()
                .filter(m -> m.getId().equals(request.getMealId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Meal", "id", request.getMealId()));

        MealOption selectedOption = meal.getOptions().stream()
                .filter(o -> o.getId().equals(request.getSelectedOptionId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("MealOption", "id", request.getSelectedOptionId()));

        // Find or create tracking record
        DailyTracking tracking = dailyTrackingRepository
                .findByClientIdAndDateAndMealId(clientId, date, meal.getId())
                .orElse(DailyTracking.builder()
                        .client(client)
                        .trackingDate(date)
                        .meal(meal)
                        .build());

        tracking.setSelectedOption(selectedOption);
        tracking.setNotes(request.getNotes());
        tracking.markCompleted();

        tracking = dailyTrackingRepository.save(tracking);
        log.info("Client '{}' tracked meal '{}' with option '{}'",
                client.getFullName(), meal.getName(), selectedOption.getName());

        return DailyMealDTO.builder()
                .mealId(meal.getId())
                .mealType(meal.getMealType())
                .name(meal.getName())
                .displayOrder(meal.getDisplayOrder())
                .isCompleted(true)
                .selectedOptionId(selectedOption.getId())
                .completedAt(tracking.getCompletedAt())
                .build();
    }

    @Transactional
    public Integer trackWater(UUID clientId, TrackWaterRequest request) {
        LocalDate date = request.getTrackingDate() != null ? request.getTrackingDate() : LocalDate.now();

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        WaterTracking tracking = WaterTracking.builder()
                .client(client)
                .trackingDate(date)
                .amountMl(request.getAmountMl())
                .loggedAt(Instant.now())
                .build();

        waterTrackingRepository.save(tracking);
        log.info("Client '{}' tracked {} ml of water", client.getFullName(), request.getAmountMl());

        return waterTrackingRepository.sumByClientIdAndDate(clientId, date);
    }

    @Transactional(readOnly = true)
    public ProgressDTO getProgress(UUID clientId, UUID dietitianId, LocalDate fromDate, LocalDate toDate) {
        // Validate client belongs to dietitian
        clientRepository.findByIdAndDietitianId(clientId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        List<Measurement> measurements = measurementRepository.findAllByClientIdOrderByDateDesc(clientId);
        List<DailyTracking> trackings = dailyTrackingRepository.findByClientIdAndDateRange(clientId, fromDate, toDate);
        int totalWater = waterTrackingRepository.sumByClientIdAndDateRange(clientId, fromDate, toDate);

        long totalDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        long daysTracked = trackings.stream()
                .map(DailyTracking::getTrackingDate)
                .distinct()
                .count();

        // Calculate weight trend
        BigDecimal startWeight = null;
        BigDecimal currentWeight = null;
        List<ProgressDTO.WeightEntry> weightHistory = new ArrayList<>();

        for (Measurement m : measurements) {
            if (m.getMeasurementDate().isAfter(fromDate.minusDays(1)) &&
                    m.getMeasurementDate().isBefore(toDate.plusDays(1))) {
                weightHistory.add(ProgressDTO.WeightEntry.builder()
                        .date(m.getMeasurementDate())
                        .weightKg(m.getWeightKg())
                        .bmi(m.getBmi())
                        .build());
            }
            if (startWeight == null || m.getMeasurementDate().isAfter(fromDate.minusDays(1))) {
                startWeight = m.getWeightKg();
            }
        }

        if (!measurements.isEmpty()) {
            currentWeight = measurements.get(0).getWeightKg();
        }

        // Meal compliance
        MealPlan activePlan = mealPlanRepository.findActiveByClientId(clientId).orElse(null);
        int mealsPerDay = activePlan != null ? activePlan.getMeals().size() : 3;
        int totalMealsExpected = (int) totalDays * mealsPerDay;
        long mealsCompleted = trackings.stream().filter(DailyTracking::getIsCompleted).count();

        // Averages
        int totalCalories = trackings.stream()
                .filter(DailyTracking::getIsCompleted)
                .mapToInt(
                        t -> t.getSelectedOption().getTotalCalories() != null ? t.getSelectedOption().getTotalCalories()
                                : 0)
                .sum();
        int avgCalories = daysTracked > 0 ? (int) (totalCalories / daysTracked) : 0;
        int avgWater = daysTracked > 0 ? (int) (totalWater / daysTracked) : 0;

        return ProgressDTO.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .startWeight(startWeight)
                .currentWeight(currentWeight)
                .weightChange(currentWeight != null && startWeight != null
                        ? currentWeight.subtract(startWeight)
                        : null)
                .weightHistory(weightHistory)
                .totalDays((int) totalDays)
                .daysTracked((int) daysTracked)
                .compliancePercentage(totalDays > 0
                        ? ((double) daysTracked / totalDays) * 100
                        : 0)
                .totalMealsExpected(totalMealsExpected)
                .mealsCompleted((int) mealsCompleted)
                .mealCompliancePercentage(totalMealsExpected > 0
                        ? ((double) mealsCompleted / totalMealsExpected) * 100
                        : 0)
                .avgDailyCalories(avgCalories)
                .avgDailyWaterMl(avgWater)
                .build();
    }
}

package com.dietary.goal.service;

import com.dietary.client.domain.Client;
import com.dietary.client.domain.Gender;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.exception.BadRequestException;
import com.dietary.common.exception.ResourceNotFoundException;
import com.dietary.goal.controller.dto.GoalDTO;
import com.dietary.goal.controller.dto.GoalRequest;
import com.dietary.goal.domain.ActivityLevel;
import com.dietary.goal.domain.Goal;
import com.dietary.goal.domain.GoalType;
import com.dietary.goal.repository.GoalRepository;
import com.dietary.measurement.domain.Measurement;
import com.dietary.measurement.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final ClientRepository clientRepository;
    private final MeasurementRepository measurementRepository;

    // Activity level multipliers for TDEE calculation
    private static final double SEDENTARY_MULTIPLIER = 1.2;
    private static final double LIGHT_MULTIPLIER = 1.375;
    private static final double MODERATE_MULTIPLIER = 1.55;
    private static final double ACTIVE_MULTIPLIER = 1.725;
    private static final double VERY_ACTIVE_MULTIPLIER = 1.9;

    // Calorie adjustment per kg of weekly weight change
    private static final int CALORIES_PER_KG = 7700; // ~1100 kcal deficit per day for 1kg/week loss

    @Transactional(readOnly = true)
    public GoalDTO getCurrentGoal(UUID clientId, UUID dietitianId) {
        Goal goal = goalRepository.findActiveByClientIdAndDietitianId(clientId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "clientId", clientId));

        GoalDTO dto = GoalDTO.fromEntity(goal);
        dto.setActivityMultiplier(getActivityMultiplier(goal.getActivityLevel()));
        return dto;
    }

    @Transactional
    public GoalDTO createOrReplaceGoal(UUID clientId, GoalRequest request, UUID dietitianId) {
        Client client = clientRepository.findByIdAndDietitianId(clientId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        // Get latest measurement for current weight
        Measurement latestMeasurement = measurementRepository.findLatestByClientId(clientId)
                .orElseThrow(() -> new BadRequestException(
                        "Client must have at least one measurement before setting a goal"));

        BigDecimal currentWeightKg = latestMeasurement.getWeightKg();
        BigDecimal heightCm = client.getHeightCm();
        Gender gender = client.getGender();
        LocalDate birthDate = client.getBirthDate();

        if (heightCm == null || gender == null || birthDate == null) {
            throw new BadRequestException("Client must have height, gender, and birth date to calculate goals");
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        // Calculate BMR using Harris-Benedict equation
        int bmr = calculateBMR(currentWeightKg.doubleValue(), heightCm.doubleValue(), age, gender);

        // Calculate TDEE
        double activityMultiplier = getActivityMultiplier(request.getActivityLevel());
        int tdee = (int) Math.round(bmr * activityMultiplier);

        // Calculate daily calorie target based on goal type
        int calorieAdjustment = 0;
        if (request.getWeeklyWeightChangeKg() != null
                && request.getWeeklyWeightChangeKg().compareTo(BigDecimal.ZERO) > 0) {
            // Weekly calorie adjustment = kg * 7700 kcal / 7 days
            calorieAdjustment = (int) Math.round(request.getWeeklyWeightChangeKg().doubleValue() * CALORIES_PER_KG / 7);
        }

        int dailyCalorieTarget = switch (request.getGoalType()) {
            case LOSE_WEIGHT -> tdee - calorieAdjustment;
            case GAIN_WEIGHT -> tdee + calorieAdjustment;
            case MAINTAIN_WEIGHT -> tdee;
        };

        // Calculate macros if not provided (default: 30% protein, 40% carbs, 30% fat)
        int proteinGrams = request.getProteinGrams() != null ? request.getProteinGrams()
                : (int) Math.round(dailyCalorieTarget * 0.30 / 4); // 4 cal per gram
        int carbsGrams = request.getCarbsGrams() != null ? request.getCarbsGrams()
                : (int) Math.round(dailyCalorieTarget * 0.40 / 4); // 4 cal per gram
        int fatGrams = request.getFatGrams() != null ? request.getFatGrams()
                : (int) Math.round(dailyCalorieTarget * 0.30 / 9); // 9 cal per gram

        // Deactivate any existing active goals
        goalRepository.deactivateAllByClientId(clientId);

        // Create new goal
        Goal goal = Goal.builder()
                .client(client)
                .goalType(request.getGoalType())
                .targetWeightKg(request.getTargetWeightKg())
                .activityLevel(request.getActivityLevel())
                .bmr(bmr)
                .tdee(tdee)
                .dailyCalorieTarget(dailyCalorieTarget)
                .proteinGrams(proteinGrams)
                .carbsGrams(carbsGrams)
                .fatGrams(fatGrams)
                .weeklyWeightChangeKg(request.getWeeklyWeightChangeKg())
                .isActive(true)
                .notes(request.getNotes())
                .build();

        goal = goalRepository.save(goal);
        log.info("Created goal for client '{}': BMR={}, TDEE={}, Target={} kcal",
                client.getFullName(), bmr, tdee, dailyCalorieTarget);

        GoalDTO dto = GoalDTO.fromEntity(goal);
        dto.setBmrFormula("Harris-Benedict");
        dto.setActivityMultiplier(activityMultiplier);
        dto.setCalorieAdjustment(calorieAdjustment);
        return dto;
    }

    /**
     * Harris-Benedict BMR formula (revised 1984)
     * Men: BMR = 88.362 + (13.397 × weight in kg) + (4.799 × height in cm) - (5.677
     * × age in years)
     * Women: BMR = 447.593 + (9.247 × weight in kg) + (3.098 × height in cm) -
     * (4.330 × age in years)
     */
    private int calculateBMR(double weightKg, double heightCm, int age, Gender gender) {
        double bmr;
        if (gender == Gender.MALE) {
            bmr = 88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age);
        } else {
            // Use female formula for FEMALE and OTHER
            bmr = 447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age);
        }
        return (int) Math.round(bmr);
    }

    private double getActivityMultiplier(ActivityLevel level) {
        return switch (level) {
            case SEDENTARY -> SEDENTARY_MULTIPLIER;
            case LIGHT -> LIGHT_MULTIPLIER;
            case MODERATE -> MODERATE_MULTIPLIER;
            case ACTIVE -> ACTIVE_MULTIPLIER;
            case VERY_ACTIVE -> VERY_ACTIVE_MULTIPLIER;
        };
    }
}

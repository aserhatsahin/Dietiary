package com.dietary.demo;

import com.dietary.auth.controller.dto.AcceptInviteRequest;
import com.dietary.auth.controller.dto.AuthResponse;
import com.dietary.auth.controller.dto.RegisterRequest;
import com.dietary.auth.domain.User;
import com.dietary.auth.repository.UserRepository;
import com.dietary.auth.service.AuthService;
import com.dietary.auth.service.ClientInviteService;
import com.dietary.client.controller.dto.ClientRequest;
import com.dietary.client.domain.Gender;
import com.dietary.client.service.ClientService;
import com.dietary.food.domain.Food;
import com.dietary.food.repository.FoodRepository;
import com.dietary.goal.controller.dto.GoalRequest;
import com.dietary.goal.domain.ActivityLevel;
import com.dietary.goal.domain.GoalType;
import com.dietary.goal.service.GoalService;
import com.dietary.mealplan.controller.dto.*;
import com.dietary.mealplan.domain.MealType;
import com.dietary.mealplan.service.MealPlanService;
import com.dietary.measurement.controller.dto.MeasurementRequest;
import com.dietary.measurement.service.MeasurementService;
import com.dietary.tracking.controller.dto.TrackMealRequest;
import com.dietary.tracking.controller.dto.TrackWaterRequest;
import com.dietary.tracking.service.TrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Creates a demo dietitian with clients, measurements, goals, a meal plan and a week of tracking,
 * so a visitor of the public demo can log in and see a populated app.
 *
 * <p>Off unless {@code app.demo.enabled=true} (env {@code APP_DEMO_ENABLED}). Runs through the real
 * services, so BMI, goals and progress are computed exactly as for real data. It is idempotent:
 * nothing happens if the demo dietitian already exists, and everything is created in one
 * transaction, so a failed run leaves nothing behind.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataSeeder {

    public static final String DIETITIAN_EMAIL = "demo@dietiary.app";
    public static final String CLIENT_WITH_ACCOUNT_EMAIL = "elif.demo@dietiary.app";
    static final int TRACKED_DAYS = 28;

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ClientService clientService;
    private final ClientInviteService clientInviteService;
    private final MeasurementService measurementService;
    private final GoalService goalService;
    private final FoodRepository foodRepository;
    private final MealPlanService mealPlanService;
    private final TrackingService trackingService;
    private final TransactionTemplate transaction;
    private final String password;

    public DemoDataSeeder(AuthService authService, UserRepository userRepository, ClientService clientService,
                          ClientInviteService clientInviteService, MeasurementService measurementService,
                          GoalService goalService, FoodRepository foodRepository, MealPlanService mealPlanService,
                          TrackingService trackingService, PlatformTransactionManager transactionManager,
                          @Value("${app.demo.password}") String password) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.clientService = clientService;
        this.clientInviteService = clientInviteService;
        this.measurementService = measurementService;
        this.goalService = goalService;
        this.foodRepository = foodRepository;
        this.mealPlanService = mealPlanService;
        this.trackingService = trackingService;
        this.transaction = new TransactionTemplate(transactionManager);
        this.password = password;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedOnStartup() {
        if (seed()) {
            log.info("Demo data created. Dietitian: {}, client with an account: {}",
                    DIETITIAN_EMAIL, CLIENT_WITH_ACCOUNT_EMAIL);
        } else {
            log.info("Demo data already present, nothing to do");
        }
    }

    /** Returns true if it created the data, false if it was already there. */
    public boolean seed() {
        if (userRepository.existsByEmail(DIETITIAN_EMAIL)) {
            return false;
        }
        transaction.executeWithoutResult(status -> createDemoData());
        return true;
    }

    private void createDemoData() {
        AuthResponse registered = authService.register(RegisterRequest.builder()
                .email(DIETITIAN_EMAIL).password(password).fullName("Demo Dietitian").build());
        UUID dietitianId = UUID.fromString(registered.getUser().getId());
        User dietitian = userRepository.findById(dietitianId).orElseThrow();
        Map<String, UUID> foods = foodRepository.findAllSystemFoods().stream()
                .collect(Collectors.toMap(Food::getName, Food::getId, (a, b) -> a));
        LocalDate today = LocalDate.now();

        // Elif: losing weight, has the app, follows an active plan
        UUID elif = createClient(dietitian, "Elif Kaya", CLIENT_WITH_ACCOUNT_EMAIL, Gender.FEMALE,
                LocalDate.of(1992, 3, 14), "165", "Lactose intolerant");
        double[] elifWeights = {78.4, 77.9, 77.1, 76.6, 76.0, 75.3, 74.8, 74.1};
        double[] elifBodyFat = {33.1, 32.8, 32.2, 31.9, 31.4, 30.9, 30.6, 30.1};
        for (int week = 0; week < elifWeights.length; week++) {
            addMeasurement(elif, dietitianId, today.minusWeeks(elifWeights.length - 1 - week),
                    elifWeights[week], elifBodyFat[week]);
        }
        setGoal(elif, dietitianId, GoalType.LOSE_WEIGHT, ActivityLevel.MODERATE, "68", "0.5");

        // Mert: losing weight, not invited yet (the invite flow can be tried on him)
        UUID mert = createClient(dietitian, "Mert Demir", "mert.demo@dietiary.app", Gender.MALE,
                LocalDate.of(1988, 11, 2), "182", null);
        double[] mertWeights = {96.0, 95.2, 94.8, 94.1, 93.6, 93.2};
        for (int week = 0; week < mertWeights.length; week++) {
            addMeasurement(mert, dietitianId, today.minusWeeks(mertWeights.length - 1 - week), mertWeights[week], null);
        }
        setGoal(mert, dietitianId, GoalType.LOSE_WEIGHT, ActivityLevel.LIGHT, "88", "0.5");

        // Zeynep: gaining weight
        UUID zeynep = createClient(dietitian, "Zeynep Aydın", "zeynep.demo@dietiary.app", Gender.FEMALE,
                LocalDate.of(1999, 7, 21), "170", "Training for a half marathon");
        double[] zeynepWeights = {54.5, 55.0, 55.4, 56.1};
        for (int week = 0; week < zeynepWeights.length; week++) {
            addMeasurement(zeynep, dietitianId, today.minusWeeks(zeynepWeights.length - 1 - week),
                    zeynepWeights[week], null);
        }
        setGoal(zeynep, dietitianId, GoalType.GAIN_WEIGHT, ActivityLevel.ACTIVE, "58", "0.25");

        // A reusable template and Elif's active plan with the same structure
        mealPlanService.createMealPlan(planRequest("Balanced 1800 kcal (template)", null, 1800, foods, true),
                dietitianId);
        MealPlanDTO plan = mealPlanService.createMealPlan(
                planRequest("Balanced 1600 kcal", elif, 1600, foods, false), dietitianId);
        mealPlanService.activateMealPlan(plan.getId(), dietitianId);

        // Elif gets an account and has tracked the last four weeks, missing about one meal in six
        String inviteToken = clientInviteService.createInvite(elif, dietitianId).getInviteToken();
        authService.acceptInvite(AcceptInviteRequest.builder().inviteToken(inviteToken).password(password).build());
        for (int daysAgo = TRACKED_DAYS; daysAgo >= 1; daysAgo--) {
            LocalDate day = today.minusDays(daysAgo);
            for (int m = 0; m < plan.getMeals().size(); m++) {
                if ((daysAgo + m) % 6 == 0) {
                    continue; // a missed meal
                }
                MealDTO meal = plan.getMeals().get(m);
                MealOptionDTO option = meal.getOptions().get((daysAgo + m) % meal.getOptions().size());
                trackingService.trackMeal(elif, TrackMealRequest.builder()
                        .mealId(meal.getId()).selectedOptionId(option.getId()).trackingDate(day).build());
            }
            int waterMl = 1250 + (daysAgo * 7 % 5) * 250; // 1.25 to 2.25 litres
            trackingService.trackWater(elif, TrackWaterRequest.builder()
                    .amountMl(waterMl).trackingDate(day).build());
        }
    }

    private UUID createClient(User dietitian, String name, String email, Gender gender, LocalDate birthDate,
                              String heightCm, String healthInfo) {
        return clientService.createClient(ClientRequest.builder()
                .fullName(name).email(email).gender(gender).birthDate(birthDate)
                .heightCm(new BigDecimal(heightCm)).healthInfo(healthInfo).build(), dietitian).getId();
    }

    private void addMeasurement(UUID clientId, UUID dietitianId, LocalDate date, double weightKg, Double bodyFat) {
        measurementService.createMeasurement(clientId, MeasurementRequest.builder()
                .measurementDate(date)
                .weightKg(BigDecimal.valueOf(weightKg))
                .bodyFatPercentage(bodyFat == null ? null : BigDecimal.valueOf(bodyFat))
                .build(), dietitianId);
    }

    private void setGoal(UUID clientId, UUID dietitianId, GoalType type, ActivityLevel activity,
                         String targetKg, String weeklyChangeKg) {
        goalService.createOrReplaceGoal(clientId, GoalRequest.builder()
                .goalType(type).activityLevel(activity)
                .targetWeightKg(new BigDecimal(targetKg))
                .weeklyWeightChangeKg(new BigDecimal(weeklyChangeKg))
                .build(), dietitianId);
    }

    private MealPlanRequest planRequest(String name, UUID clientId, int calories, Map<String, UUID> foods,
                                        boolean template) {
        // Quantities are in each food's own serving unit: calories = quantity / servingSize * kcal per serving
        Function<Object[], MealOptionItemRequest> item = spec -> MealOptionItemRequest.builder()
                .foodId(requireFood(foods, (String) spec[0]))
                .quantity(new BigDecimal((String) spec[1]))
                .quantityUnit((String) spec[2])
                .build();
        return MealPlanRequest.builder()
                .name(name)
                .description("Two alternatives per meal; pick one each day.")
                .clientId(clientId)
                .isTemplate(template)
                .dailyCalories(calories)
                .meals(List.of(
                        meal(MealType.BREAKFAST, "Breakfast",
                                option("Oats and berries", item,
                                        new Object[]{"Oatmeal (Cooked)", "200", "g"},
                                        new Object[]{"Blueberries", "100", "g"},
                                        new Object[]{"Almonds", "28", "g"}),
                                option("Eggs on toast", item,
                                        new Object[]{"Eggs (Whole, Boiled)", "2", "large"},
                                        new Object[]{"Whole Wheat Bread", "1", "slice"},
                                        new Object[]{"Tomatoes (Raw)", "100", "g"})),
                        meal(MealType.LUNCH, "Lunch",
                                option("Chicken and rice", item,
                                        new Object[]{"Chicken Breast (Grilled)", "150", "g"},
                                        new Object[]{"Brown Rice (Cooked)", "150", "g"},
                                        new Object[]{"Broccoli (Steamed)", "100", "g"}),
                                option("Salmon and quinoa", item,
                                        new Object[]{"Salmon (Baked)", "120", "g"},
                                        new Object[]{"Quinoa (Cooked)", "150", "g"},
                                        new Object[]{"Spinach (Raw)", "50", "g"})),
                        meal(MealType.AFTERNOON_SNACK, "Afternoon snack",
                                option("Yogurt and strawberries", item,
                                        new Object[]{"Greek Yogurt (Plain, Non-fat)", "170", "g"},
                                        new Object[]{"Strawberries", "100", "g"}),
                                option("Apple and walnuts", item,
                                        new Object[]{"Apple", "1", "medium"},
                                        new Object[]{"Walnuts", "28", "g"})),
                        meal(MealType.DINNER, "Dinner",
                                option("Salmon and sweet potato", item,
                                        new Object[]{"Salmon (Baked)", "120", "g"},
                                        new Object[]{"Sweet Potato (Baked)", "150", "g"},
                                        new Object[]{"Broccoli (Steamed)", "100", "g"}),
                                option("Chicken and vegetables", item,
                                        new Object[]{"Chicken Breast (Grilled)", "120", "g"},
                                        new Object[]{"Carrots (Raw)", "100", "g"},
                                        new Object[]{"Olive Oil", "1", "tbsp"}))))
                .build();
    }

    private static MealRequest meal(MealType type, String name, MealOptionRequest... options) {
        return MealRequest.builder().mealType(type).name(name).options(List.of(options)).build();
    }

    private static MealOptionRequest option(String name, Function<Object[], MealOptionItemRequest> item,
                                            Object[]... items) {
        return MealOptionRequest.builder().name(name)
                .items(java.util.Arrays.stream(items).map(item).toList()).build();
    }

    private static UUID requireFood(Map<String, UUID> foods, String name) {
        UUID id = foods.get(name);
        if (id == null) {
            throw new IllegalStateException("System food missing for demo data: " + name);
        }
        return id;
    }
}

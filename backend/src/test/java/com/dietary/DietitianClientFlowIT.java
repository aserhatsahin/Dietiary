package com.dietary;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end API flows over HTTP against a real database. */
class DietitianClientFlowIT extends IntegrationTestBase {

    @Test
    void fullDietitianToClientJourney() throws Exception {
        String dietitian = registerDietitian();
        String clientEmail = uniqueEmail("client");
        String clientId = createClient(dietitian, clientEmail);

        // Measurement: BMI is calculated from weight and the client's height
        post("/api/clients/" + clientId + "/measurements", dietitian, Map.of(
                "measurementDate", LocalDate.now().toString(),
                "weightKg", 70.0))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bmi", closeTo(25.71, 0.01)));

        // Goal: BMR / TDEE / calorie target are calculated
        post("/api/clients/" + clientId + "/goals", dietitian, Map.of(
                "goalType", "LOSE_WEIGHT",
                "activityLevel", "MODERATE",
                "targetWeightKg", 65.0,
                "weeklyWeightChangeKg", 0.5))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bmr", greaterThan(0)))
                .andExpect(jsonPath("$.data.dailyCalorieTarget", greaterThan(0)));

        String foodId = createFood(dietitian, "Oatmeal", "Grains");

        // Meal plan with one meal and two alternatives, assigned to the client
        var plan = post("/api/meal-plans", dietitian, Map.of(
                "name", "Week 1",
                "clientId", clientId,
                "dailyCalories", 1800,
                "meals", List.of(Map.of(
                        "mealType", "BREAKFAST",
                        "name", "Breakfast",
                        "options", List.of(
                                Map.of("name", "Option A", "items", List.of(
                                        Map.of("foodId", foodId, "quantity", 1, "quantityUnit", "serving"))),
                                Map.of("name", "Option B", "items", List.of(
                                        Map.of("foodId", foodId, "quantity", 2, "quantityUnit", "serving"))))))))
                .andExpect(status().isCreated());
        String planId = read(plan, "$.data.id");
        String mealId = read(plan, "$.data.meals[0].id");
        String optionB = read(plan, "$.data.meals[0].options[1].id");

        post("/api/meal-plans/" + planId + "/activate", dietitian, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(true));

        // Invite -> accept -> client login
        var invite = post("/api/clients/" + clientId + "/invite", dietitian, null)
                .andExpect(status().isCreated());
        String inviteToken = read(invite, "$.data.inviteToken");

        post("/api/auth/client/accept-invite", null, Map.of("inviteToken", inviteToken, "password", PASSWORD))
                .andExpect(status().isCreated());

        var login = post("/api/auth/login", null, Map.of("email", clientEmail, "password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.role").value("CLIENT"));
        String client = read(login, "$.data.accessToken");

        // Client sees today's plan, picks option B, drinks water
        getAs("/api/mobile/tracking/daily", client)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mealPlanId").value(planId))
                .andExpect(jsonPath("$.data.meals", hasSize(1)))
                .andExpect(jsonPath("$.data.meals[0].options", hasSize(2)));

        post("/api/mobile/tracking/meals", client, Map.of("mealId", mealId, "selectedOptionId", optionB))
                .andExpect(status().isOk());
        post("/api/mobile/tracking/water", client, Map.of("amountMl", 500))
                .andExpect(status().isOk());

        getAs("/api/mobile/tracking/daily", client)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meals[0].selectedOptionId").value(optionB))
                .andExpect(jsonPath("$.data.totalWaterMl").value(500));

        // Dietitian sees the client's progress
        String today = LocalDate.now().toString();
        getAs("/api/clients/" + clientId + "/progress?fromDate=" + today + "&toDate=" + today, dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mealsCompleted").value(1))
                .andExpect(jsonPath("$.data.avgDailyWaterMl").value(500))
                .andExpect(jsonPath("$.data.currentWeight").value(70.0));
    }

    @Test
    void dietitianCannotSeeAnotherDietitiansClient() throws Exception {
        String owner = registerDietitian();
        String other = registerDietitian();
        String clientId = createClient(owner, uniqueEmail("client"));

        getAs("/api/clients/" + clientId, other).andExpect(status().isNotFound());
        getAs("/api/clients/" + clientId + "/measurements", other).andExpect(status().isNotFound());
        post("/api/clients/" + clientId + "/invite", other, null).andExpect(status().isNotFound());
        getAs("/api/clients", other)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", empty()));
    }

    @Test
    void clientRoleIsForbiddenFromDietitianEndpoints() throws Exception {
        String dietitian = registerDietitian();
        String email = uniqueEmail("client");
        String clientId = createClient(dietitian, email);
        String inviteToken = read(post("/api/clients/" + clientId + "/invite", dietitian, null), "$.data.inviteToken");
        String client = read(post("/api/auth/client/accept-invite", null,
                Map.of("inviteToken", inviteToken, "password", PASSWORD)), "$.data.accessToken");

        getAs("/api/clients", client).andExpect(status().isForbidden());
        post("/api/clients/" + clientId + "/invite", client, null).andExpect(status().isForbidden());
    }

    @Test
    void foodSearchWorksWithAndWithoutFilters() throws Exception {
        String dietitian = registerDietitian();
        createFood(dietitian, "Lentil Soup", "Soups");
        createFood(dietitian, "Grilled Trout", "Fish");

        getAs("/api/foods/search", dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", hasItems("Lentil Soup", "Grilled Trout")));
        getAs("/api/foods/search?query=lentil", dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", contains("Lentil Soup")));
        getAs("/api/foods/search?category=fish", dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", contains("Grilled Trout")));

        // Custom foods are private to the dietitian who created them
        getAs("/api/foods/search?query=lentil", registerDietitian())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", empty()));
    }

    @Test
    void malformedRequestsReturn400Not500() throws Exception {
        String dietitian = registerDietitian();
        String clientId = createClient(dietitian, uniqueEmail("client"));

        getAs("/api/clients/" + clientId + "/progress", dietitian)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("fromDate")));
        getAs("/api/clients/" + clientId + "/progress?fromDate=yesterday&toDate=today", dietitian)
                .andExpect(status().isBadRequest());
        getAs("/api/clients/not-a-uuid", dietitian)
                .andExpect(status().isBadRequest());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/clients")
                        .header("Authorization", "Bearer " + dietitian)
                        .contentType("application/json")
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        getAs("/api/clients", null).andExpect(status().isUnauthorized());
    }

    @Test
    void mealPlanTemplatesCanBeCreatedAndListed() throws Exception {
        String dietitian = registerDietitian();

        post("/api/meal-plans", dietitian, Map.of("name", "Standard 1600 kcal", "isTemplate", true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isTemplate").value(true));

        getAs("/api/meal-plans/templates", dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", hasItem("Standard 1600 kcal")));
    }
}

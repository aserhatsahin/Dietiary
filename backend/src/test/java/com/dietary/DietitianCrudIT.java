package com.dietary;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Update/delete/history endpoints on the dietitian side. */
class DietitianCrudIT extends IntegrationTestBase {

    private static final LocalDate TODAY = LocalDate.now();

    private ResultActions putAs(String url, String token, Object body) throws Exception {
        return mvc.perform(put(url).header("Authorization", "Bearer " + token)
                .contentType("application/json").content(json(body)));
    }

    private ResultActions deleteAs(String url, String token) throws Exception {
        return mvc.perform(delete(url).header("Authorization", "Bearer " + token));
    }

    private String addMeasurement(String dietitian, String clientId, LocalDate date, double weightKg) throws Exception {
        return read(post("/api/clients/" + clientId + "/measurements", dietitian,
                Map.of("measurementDate", date.toString(), "weightKg", weightKg))
                .andExpect(status().isCreated()), "$.data.id");
    }

    private ResultActions createPlan(String dietitian, String clientId, String foodId) throws Exception {
        return post("/api/meal-plans", dietitian, Map.of(
                "name", "Plan " + System.nanoTime(),
                "clientId", clientId,
                "meals", List.of(Map.of(
                        "mealType", "LUNCH",
                        "name", "Lunch",
                        "options", List.of(Map.of("name", "A", "items", List.of(
                                Map.of("foodId", foodId, "quantity", 1, "quantityUnit", "serving"))))))))
                .andExpect(status().isCreated());
    }

    // ---------- Measurements

    @Test
    void deltasCompareAgainstThePreviousMeasurementByDate() throws Exception {
        String dietitian = registerDietitian();
        String clientId = createClient(dietitian, uniqueEmail("client"));
        addMeasurement(dietitian, clientId, TODAY.minusDays(20), 80.0);
        addMeasurement(dietitian, clientId, TODAY, 76.0);

        // Back-dated entry between the two: compared with the older one, not the newest
        post("/api/clients/" + clientId + "/measurements", dietitian,
                Map.of("measurementDate", TODAY.minusDays(10).toString(), "weightKg", 78.0))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.weightDelta").value(-2.0));

        getAs("/api/clients/" + clientId + "/measurements", dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].weightKg", contains(76.0, 78.0, 80.0)))
                .andExpect(jsonPath("$.data[0].weightDelta").value(-2.0))
                .andExpect(jsonPath("$.data[1].weightDelta").value(-2.0))
                .andExpect(jsonPath("$.data[2].weightDelta").doesNotExist());

        getAs("/api/clients/" + clientId + "/measurements/latest", dietitian)
                .andExpect(jsonPath("$.data.weightKg").value(76.0))
                .andExpect(jsonPath("$.data.weightDelta").value(-2.0));
    }

    @Test
    void measurementCanBeUpdatedAndBmiIsRecalculated() throws Exception {
        String dietitian = registerDietitian();
        String clientId = createClient(dietitian, uniqueEmail("client")); // height 165 cm
        String measurementId = addMeasurement(dietitian, clientId, TODAY, 70.0);

        putAs("/api/clients/" + clientId + "/measurements/" + measurementId, dietitian,
                Map.of("measurementDate", TODAY.toString(), "weightKg", 68.0, "notes", "corrected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weightKg").value(68.0))
                .andExpect(jsonPath("$.data.bmi", closeTo(24.98, 0.01)))
                .andExpect(jsonPath("$.data.notes").value("corrected"));
    }

    @Test
    void measurementCanBeDeleted() throws Exception {
        String dietitian = registerDietitian();
        String clientId = createClient(dietitian, uniqueEmail("client"));
        String keep = addMeasurement(dietitian, clientId, TODAY.minusDays(1), 70.0);
        String remove = addMeasurement(dietitian, clientId, TODAY, 69.0);

        deleteAs("/api/clients/" + clientId + "/measurements/" + remove, dietitian).andExpect(status().isOk());

        getAs("/api/clients/" + clientId + "/measurements", dietitian)
                .andExpect(jsonPath("$.data[*].id", contains(keep)));
        deleteAs("/api/clients/" + clientId + "/measurements/" + remove, dietitian).andExpect(status().isNotFound());
    }

    @Test
    void measurementsAreScopedToTheirClientAndDietitian() throws Exception {
        String owner = registerDietitian();
        String other = registerDietitian();
        String clientId = createClient(owner, uniqueEmail("client"));
        String otherClientId = createClient(owner, uniqueEmail("client"));
        String measurementId = addMeasurement(owner, clientId, TODAY, 70.0);
        Map<String, Object> body = Map.of("measurementDate", TODAY.toString(), "weightKg", 1.0);

        // Another dietitian
        putAs("/api/clients/" + clientId + "/measurements/" + measurementId, other, body)
                .andExpect(status().isNotFound());
        deleteAs("/api/clients/" + clientId + "/measurements/" + measurementId, other)
                .andExpect(status().isNotFound());
        // Same dietitian, wrong client in the URL
        putAs("/api/clients/" + otherClientId + "/measurements/" + measurementId, owner, body)
                .andExpect(status().isNotFound());

        getAs("/api/clients/" + clientId + "/measurements/latest", owner)
                .andExpect(jsonPath("$.data.weightKg").value(70.0));
    }

    // ---------- Meal plans

    @Test
    void clientMealPlansCanBeListedAndUntrackedPlansDeleted() throws Exception {
        String dietitian = registerDietitian();
        String clientId = createClient(dietitian, uniqueEmail("client"));
        String foodId = createFood(dietitian, "Rice", "Grains");
        String first = read(createPlan(dietitian, clientId, foodId), "$.data.id");
        String second = read(createPlan(dietitian, clientId, foodId), "$.data.id");

        getAs("/api/clients/" + clientId + "/meal-plans", dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", containsInAnyOrder(first, second)));

        deleteAs("/api/meal-plans/" + first, dietitian).andExpect(status().isOk());
        getAs("/api/meal-plans/" + first, dietitian).andExpect(status().isNotFound());
        getAs("/api/clients/" + clientId + "/meal-plans", dietitian)
                .andExpect(jsonPath("$.data[*].id", contains(second)));

        // Templates too, and only by their owner
        String template = read(post("/api/meal-plans", dietitian, Map.of("name", "Template", "isTemplate", true))
                .andExpect(status().isCreated()), "$.data.id");
        deleteAs("/api/meal-plans/" + template, registerDietitian()).andExpect(status().isNotFound());
        deleteAs("/api/meal-plans/" + template, dietitian).andExpect(status().isOk());
    }

    @Test
    void trackedPlanCannotBeDeletedOrHaveItsMealsReplaced() throws Exception {
        String dietitian = registerDietitian();
        String clientId = createClient(dietitian, uniqueEmail("client"));
        String foodId = createFood(dietitian, "Rice", "Grains");
        var plan = createPlan(dietitian, clientId, foodId);
        String planId = read(plan, "$.data.id");
        String mealId = read(plan, "$.data.meals[0].id");
        String optionId = read(plan, "$.data.meals[0].options[0].id");
        post("/api/meal-plans/" + planId + "/activate", dietitian, null).andExpect(status().isOk());

        String client = onboardClient(dietitian, clientId);
        post("/api/mobile/tracking/meals", client, Map.of("mealId", mealId, "selectedOptionId", optionId))
                .andExpect(status().isOk());

        deleteAs("/api/meal-plans/" + planId, dietitian)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("tracking history")));

        putAs("/api/meal-plans/" + planId, dietitian, Map.of("name", "New meals", "meals", List.of(Map.of(
                "mealType", "DINNER", "name", "Dinner", "options", List.of()))))
                .andExpect(status().isConflict());

        // Renaming without touching meals is still allowed
        putAs("/api/meal-plans/" + planId, dietitian, Map.of("name", "Renamed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Renamed"))
                .andExpect(jsonPath("$.data.meals", hasSize(1)));
    }

    // ---------- Goals

    @Test
    void goalHistoryListsAllGoalsNewestFirst() throws Exception {
        String dietitian = registerDietitian();
        String clientId = createClient(dietitian, uniqueEmail("client"));
        addMeasurement(dietitian, clientId, TODAY, 80.0);

        post("/api/clients/" + clientId + "/goals", dietitian,
                Map.of("goalType", "LOSE_WEIGHT", "activityLevel", "LIGHT"))
                .andExpect(status().isCreated());
        post("/api/clients/" + clientId + "/goals", dietitian,
                Map.of("goalType", "MAINTAIN_WEIGHT", "activityLevel", "MODERATE"))
                .andExpect(status().isCreated());

        getAs("/api/clients/" + clientId + "/goals", dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].goalType").value("MAINTAIN_WEIGHT"))
                .andExpect(jsonPath("$.data[0].isActive").value(true))
                .andExpect(jsonPath("$.data[1].goalType").value("LOSE_WEIGHT"))
                .andExpect(jsonPath("$.data[1].isActive").value(false));

        getAs("/api/clients/" + clientId + "/goals", registerDietitian()).andExpect(status().isNotFound());
    }
}

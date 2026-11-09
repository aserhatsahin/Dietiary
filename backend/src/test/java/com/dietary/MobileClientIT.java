package com.dietary;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The client's read-only view of their own data under /api/mobile. */
class MobileClientIT extends IntegrationTestBase {

    private void addMeasurement(String dietitian, String clientId, LocalDate date, double weightKg) throws Exception {
        post("/api/clients/" + clientId + "/measurements", dietitian, Map.of(
                "measurementDate", date.toString(),
                "weightKg", weightKg))
                .andExpect(status().isCreated());
    }

    @Test
    void clientReadsOwnProfileMeasurementsGoalAndProgress() throws Exception {
        String dietitian = registerDietitian();
        String email = uniqueEmail("client");
        String clientId = createClient(dietitian, email);
        LocalDate today = LocalDate.now();
        addMeasurement(dietitian, clientId, today.minusDays(14), 72.0);
        addMeasurement(dietitian, clientId, today, 70.5);
        post("/api/clients/" + clientId + "/goals", dietitian, Map.of(
                "goalType", "LOSE_WEIGHT", "activityLevel", "LIGHT", "targetWeightKg", 65.0))
                .andExpect(status().isCreated());
        String client = onboardClient(dietitian, clientId);

        getAs("/api/mobile/me", client)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(clientId))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.hasAccount").value(true));

        getAs("/api/mobile/measurements", client)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].weightKg").value(70.5));

        getAs("/api/mobile/measurements/latest", client)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weightKg").value(70.5));

        getAs("/api/mobile/goal", client)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goalType").value("LOSE_WEIGHT"))
                .andExpect(jsonPath("$.data.dailyCalorieTarget", greaterThan(0)));

        // Default range is the last 30 days, which includes both measurements
        getAs("/api/mobile/progress", client)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.toDate").value(today.toString()))
                .andExpect(jsonPath("$.data.fromDate").value(today.minusDays(29).toString()))
                .andExpect(jsonPath("$.data.weightHistory", hasSize(2)))
                .andExpect(jsonPath("$.data.currentWeight").value(70.5));
    }

    @Test
    void eachClientSeesOnlyTheirOwnData() throws Exception {
        String dietitian = registerDietitian();
        String clientA = createClient(dietitian, uniqueEmail("a"));
        String clientB = createClient(dietitian, uniqueEmail("b"));
        addMeasurement(dietitian, clientA, LocalDate.now(), 60.0);
        addMeasurement(dietitian, clientB, LocalDate.now(), 90.0);
        String tokenA = onboardClient(dietitian, clientA);
        String tokenB = onboardClient(dietitian, clientB);

        getAs("/api/mobile/measurements", tokenA)
                .andExpect(jsonPath("$.data[*].weightKg", contains(60.0)));
        getAs("/api/mobile/measurements", tokenB)
                .andExpect(jsonPath("$.data[*].weightKg", contains(90.0)));
        getAs("/api/mobile/me", tokenA).andExpect(jsonPath("$.data.id").value(clientA));
    }

    @Test
    void dietitiansCannotUseMobileEndpoints() throws Exception {
        String dietitian = registerDietitian();
        getAs("/api/mobile/me", dietitian).andExpect(status().isForbidden());
        getAs("/api/mobile/tracking/daily", dietitian).andExpect(status().isForbidden());
    }

    @Test
    void clientWithoutGoalOrMeasurementsGets404() throws Exception {
        String dietitian = registerDietitian();
        String client = onboardClient(dietitian, createClient(dietitian, uniqueEmail("client")));

        getAs("/api/mobile/goal", client).andExpect(status().isNotFound());
        getAs("/api/mobile/measurements/latest", client).andExpect(status().isNotFound());
        getAs("/api/mobile/measurements", client)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", empty()));
    }

    @Test
    void invalidProgressRangeIs400() throws Exception {
        String dietitian = registerDietitian();
        String client = onboardClient(dietitian, createClient(dietitian, uniqueEmail("client")));

        getAs("/api/mobile/progress?fromDate=2026-02-01&toDate=2026-01-01", client)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("fromDate")));
    }

    @Test
    void clientWhoseProfileWasDeletedIsForbidden() throws Exception {
        String dietitian = registerDietitian();
        String clientId = createClient(dietitian, uniqueEmail("client"));
        String client = onboardClient(dietitian, clientId);
        getAs("/api/mobile/me", client).andExpect(status().isOk());

        mvc.perform(delete("/api/clients/" + clientId).header("Authorization", "Bearer " + dietitian))
                .andExpect(status().isOk());

        getAs("/api/mobile/me", client).andExpect(status().isForbidden());
        getAs("/api/mobile/tracking/daily", client).andExpect(status().isForbidden());
        post("/api/mobile/tracking/water", client, Map.of("amountMl", 250)).andExpect(status().isForbidden());
    }
}

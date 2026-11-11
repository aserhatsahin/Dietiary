package com.dietary;

import com.dietary.demo.DemoDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The public demo's data: created at startup when app.demo.enabled=true. */
@TestPropertySource(properties = {"app.demo.enabled=true", "app.demo.password=Demo-test-password-1"})
class DemoDataSeederIT extends IntegrationTestBase {

    private static final String DEMO_PASSWORD = "Demo-test-password-1";

    @Autowired
    private DemoDataSeeder seeder;

    private String login(String email) throws Exception {
        return read(post("/api/auth/login", null, Map.of("email", email, "password", DEMO_PASSWORD))
                .andExpect(status().isOk()), "$.data.accessToken");
    }

    @Test
    void demoDietitianSeesThreeClientsWithHistory() throws Exception {
        String dietitian = login(DemoDataSeeder.DIETITIAN_EMAIL);

        var clients = getAs("/api/clients", dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[*].fullName", hasItems("Elif Kaya", "Mert Demir", "Zeynep Aydın")));
        String elifId = read(clients, "$.data[?(@.fullName == 'Elif Kaya')].id").replaceAll("[\\[\\]\"]", "");

        getAs("/api/clients/" + elifId + "/measurements", dietitian)
                .andExpect(jsonPath("$.data", hasSize(8)))
                .andExpect(jsonPath("$.data[0].weightKg").value(74.1))
                .andExpect(jsonPath("$.data[0].weightDelta").value(-0.7));
        getAs("/api/clients/" + elifId + "/goals/current", dietitian)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goalType").value("LOSE_WEIGHT"));
        getAs("/api/meal-plans/templates", dietitian)
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void demoClientSeesActivePlanAndTrackedWeek() throws Exception {
        String elif = login(DemoDataSeeder.CLIENT_WITH_ACCOUNT_EMAIL);

        getAs("/api/mobile/tracking/daily", elif)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meals", hasSize(4)))
                .andExpect(jsonPath("$.data.meals[*].options[*].totalCalories", everyItem(greaterThan(100))));

        getAs("/api/mobile/progress", elif)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentWeight").value(74.1))
                // Four tracked weeks out of the default 30-day window, about five in six meals eaten
                .andExpect(jsonPath("$.data.mealsCompleted", greaterThan(80)))
                .andExpect(jsonPath("$.data.mealCompliancePercentage", greaterThan(70.0)))
                .andExpect(jsonPath("$.data.avgDailyWaterMl", greaterThan(1000)));
    }

    @Test
    void seedingTwiceCreatesNothingNew() throws Exception {
        assertThat(seeder.seed()).isFalse();
        getAs("/api/clients", login(DemoDataSeeder.DIETITIAN_EMAIL))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }
}

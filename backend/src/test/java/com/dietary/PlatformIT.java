package com.dietary;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Health checks and CORS: what a hosting platform and the web panel rely on. */
class PlatformIT extends IntegrationTestBase {

    @Test
    void healthEndpointsArePublic() throws Exception {
        getAs("/actuator/health", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        getAs("/actuator/health/liveness", null).andExpect(status().isOk());
        getAs("/actuator/health/readiness", null).andExpect(status().isOk());
    }

    @Test
    void systemFoodsAreSeededByTheMigration() throws Exception {
        String token = registerDietitian();
        getAs("/api/foods/search?source=SYSTEM", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(25)))
                .andExpect(jsonPath("$.data[*].name", org.hamcrest.Matchers.hasItem("Chicken Breast (Grilled)")));
        getAs("/api/foods/categories", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasItems("Protein", "Grains", "Fruits")));
    }

    @Test
    void otherActuatorEndpointsAreNotExposed() throws Exception {
        // Not exposed at all, so even an authenticated user would not find them
        getAs("/actuator/env", registerDietitian()).andExpect(status().isNotFound());
    }

    @Test
    void unknownUrlsAndWrongMethodsKeepTheirStatusCodes() throws Exception {
        String token = registerDietitian();
        getAs("/api/does-not-exist", token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/foods/search")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void corsAllowsTheConfiguredWebPanelOrigin() throws Exception {
        mvc.perform(options("/api/clients")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")));
    }

    @Test
    void corsRejectsOtherOrigins() throws Exception {
        mvc.perform(options("/api/clients")
                        .header("Origin", "https://evil.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}

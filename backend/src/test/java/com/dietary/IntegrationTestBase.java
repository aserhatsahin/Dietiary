package com.dietary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the whole app against a real PostgreSQL in Docker. Flyway creates the schema
 * and Hibernate validates it, so a migration that drifts from the entities fails here.
 * Skipped when Docker is not available.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTestBase {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18");

    protected static final String PASSWORD = "Integration-test-pw1";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    protected static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@example.test";
    }

    protected ResultActions post(String url, String token, Object body) throws Exception {
        var req = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body == null ? "{}" : json(body));
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(req);
    }

    protected ResultActions getAs(String url, String token) throws Exception {
        var req = get(url);
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(req);
    }

    protected static String read(ResultActions result, String path) throws Exception {
        Object value = JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
        return String.valueOf(value);
    }

    /** Registers a dietitian and returns the access token. */
    protected String registerDietitian() throws Exception {
        var result = post("/api/auth/register", null, Map.of(
                "email", uniqueEmail("dyt"),
                "password", PASSWORD,
                "fullName", "Test Dietitian"))
                .andExpect(status().is2xxSuccessful());
        return read(result, "$.data.accessToken");
    }

    /** Creates a custom food for the dietitian and returns the food id. */
    protected String createFood(String dietitianToken, String name, String category) throws Exception {
        var result = post("/api/foods", dietitianToken, Map.of(
                "name", name,
                "servingSize", "100",
                "servingUnit", "g",
                "caloriesPerServing", 150,
                "proteinGrams", 10,
                "category", category))
                .andExpect(status().isCreated());
        return read(result, "$.data.id");
    }

    /** Creates a client for the dietitian and returns the client id. */
    protected String createClient(String dietitianToken, String email) throws Exception {
        var result = post("/api/clients", dietitianToken, Map.of(
                "fullName", "Test Client",
                "email", email,
                "gender", "FEMALE",
                "heightCm", 165,
                "birthDate", "1995-04-01"))
                .andExpect(status().isCreated());
        return read(result, "$.data.id");
    }
}

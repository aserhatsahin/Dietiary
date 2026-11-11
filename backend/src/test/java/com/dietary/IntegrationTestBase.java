package com.dietary;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the whole app against a real PostgreSQL in Docker. Flyway creates the schema
 * and Hibernate validates it, so a migration that drifts from the entities fails here.
 * Skipped when Docker is not available.
 *
 * <p>One container is shared by every test class for the whole run. A per-class container would be
 * stopped after the first class while Spring keeps reusing the cached context that points at it.
 * Testcontainers' Ryuk sidecar removes the container when the JVM exits.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTestBase {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18");

    static {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            POSTGRES.start();
        }
    }

    protected static final String PASSWORD = "Integration-test-pw1";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JsonMapper jsonMapper;

    protected String json(Object body) throws Exception {
        return jsonMapper.writeValueAsString(body);
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

    /** Invites the client, accepts the invite and returns the client's access token. */
    protected String onboardClient(String dietitianToken, String clientId) throws Exception {
        String inviteToken = read(post("/api/clients/" + clientId + "/invite", dietitianToken, null)
                .andExpect(status().isCreated()), "$.data.inviteToken");
        return read(post("/api/auth/client/accept-invite", null,
                Map.of("inviteToken", inviteToken, "password", PASSWORD))
                .andExpect(status().isCreated()), "$.data.accessToken");
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

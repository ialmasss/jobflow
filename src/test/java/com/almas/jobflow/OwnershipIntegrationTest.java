package com.almas.jobflow;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OwnershipIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userCannotSeeChangeOrDeleteAnotherUsersApplication() throws Exception {
        String aliceToken = register("alice@test.com", "alice");
        String bobToken = register("bob@test.com", "bob");
        long applicationId = createApplication(aliceToken);

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Kaspi"));

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyName":"Hacked","jobTitle":"Hacked","appliedDate":"2026-11-20"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/applications")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Kaspi"));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registeringTheSameEmailTwiceReturnsConflict() throws Exception {
        register("carol@test.com", "carol");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"carol@test.com","username":"carol2","password":"password123"}
                                """))
                .andExpect(status().isConflict());
    }

    private String register(String email, String username) throws Exception {
        String body = """
                {"email":"%s","username":"%s","password":"password123"}
                """.formatted(email, username);

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.token");
    }

    private long createApplication(String token) throws Exception {
        String body = """
                {"companyName":"Kaspi","jobTitle":"Java Intern","appliedDate":"2026-11-20"}
                """;

        String response = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }
}
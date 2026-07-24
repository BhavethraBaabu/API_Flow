package com.apiflow.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLogin_returnsTokens() throws Exception {
        Map<String, Object> registerPayload = Map.of(
                "fullName", "Ada Lovelace",
                "email", "ada@apiflow.dev",
                "password", "SuperSecret123",
                "role", "ENGINEER"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.email").value("ada@apiflow.dev"));

        Map<String, String> loginPayload = Map.of(
                "email", "ada@apiflow.dev",
                "password", "SuperSecret123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        Map<String, Object> registerPayload = Map.of(
                "fullName", "Grace Hopper",
                "email", "grace@apiflow.dev",
                "password", "CorrectPassword1",
                "role", "ADMIN"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isCreated());

        Map<String, String> badLogin = Map.of(
                "email", "grace@apiflow.dev",
                "password", "WrongPassword"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }
}

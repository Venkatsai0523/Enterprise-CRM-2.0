package com.crm.identity;

import com.crm.identity.api.dto.JwtResponseDto;
import com.crm.identity.api.dto.LoginRequestDto;
import com.crm.identity.api.dto.UserRegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete Auth Flow: Register -> Login -> Role-Protected Endpoint Access")
    void fullAuthLifecycleTest() throws Exception {
        // 1. Register Admin User
        UserRegistrationDto adminReg = UserRegistrationDto.builder()
                .email("admin-test@nexus.com")
                .password("adminSecret123")
                .firstName("Alice")
                .lastName("Admin")
                .roleName("ROLE_ADMIN")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin-test@nexus.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));

        // 2. Register Sales Rep User
        UserRegistrationDto repReg = UserRegistrationDto.builder()
                .email("rep-test@nexus.com")
                .password("repSecret123")
                .firstName("Bob")
                .lastName("Rep")
                .roleName("ROLE_SALES_REP")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repReg)))
                .andExpect(status().isCreated());

        // 3. Login Admin to get JWT token
        LoginRequestDto adminLogin = LoginRequestDto.builder()
                .email("admin-test@nexus.com")
                .password("adminSecret123")
                .build();

        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String adminToken = objectMapper.readValue(adminLoginResult.getResponse().getContentAsString(), JwtResponseDto.class).getToken();

        // 4. Login Sales Rep to get JWT token
        LoginRequestDto repLogin = LoginRequestDto.builder()
                .email("rep-test@nexus.com")
                .password("repSecret123")
                .build();

        MvcResult repLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String repToken = objectMapper.readValue(repLoginResult.getResponse().getContentAsString(), JwtResponseDto.class).getToken();

        // 5. Unauthenticated request to protected endpoint -> 401 Unauthorized
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized());

        // 6. Admin accesses Admin endpoint -> 200 OK
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome Admin! Access granted."));

        // 7. Sales Rep accesses Admin endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + repToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        // 8. Sales Rep accesses Sales Rep endpoint -> 200 OK
        mockMvc.perform(get("/api/test/sales-rep")
                        .header("Authorization", "Bearer " + repToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome Sales Representative! Access granted."));
    }
}

package com.crm;

import com.crm.identity.api.dto.JwtResponseDto;
import com.crm.identity.api.dto.LoginRequestDto;
import com.crm.identity.api.dto.UserRegistrationDto;
import com.crm.identity.service.AuthService;
import com.crm.lead.api.dto.LeadCreateDto;
import com.crm.organization.api.OrganizationApi;
import com.crm.organization.api.dto.OrganizationCreateDto;
import com.crm.organization.api.dto.OrganizationResponseDto;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class OpenApiGapsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationApi organizationApi;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Verify Subdomain Lookup, Profile Retrieval, Pagination Caps, Idempotency, and Soft Deletes")
    void testOpenApiGaps() throws Exception {
        // ==========================================
        // 1. Register user — this also creates the org with a unique subdomain
        // ==========================================
        String subdomain = "test-subdomain-" + UUID.randomUUID().toString().substring(0, 8);
        String email = "test-user-" + UUID.randomUUID() + "@gaptest.com";
        authService.register(UserRegistrationDto.builder()
                .email(email)
                .password("password123")
                .firstName("Alice")
                .lastName("Tester")
                .roleName("ROLE_ADMIN")
                .organizationName("Gap Testing Org")
                .subdomain(subdomain)
                .build());

        OrganizationResponseDto org = organizationApi.findOrganizationBySubdomain(subdomain)
                .orElseThrow(() -> new RuntimeException("Org not found"));

        // ==========================================
        // 2. Test Subdomain Lookup (Public, Pre-auth)
        // ==========================================
        mockMvc.perform(get("/api/organizations/lookup")
                        .param("subdomain", subdomain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(org.getId().toString()))
                .andExpect(jsonPath("$.data.name").value("Gap Testing Org"));

        JwtResponseDto loginResponse = authService.login(LoginRequestDto.builder()
                .email(email)
                .password("password123")
                .build());
        String jwt = "Bearer " + loginResponse.getToken();

        // ==========================================
        // 3. Test Profile Retrieval (GET /api/users/me)
        // ==========================================
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.firstName").value("Alice"));

        // ==========================================
        // 4. Test Pagination Cap (Max 100 size limit)
        // ==========================================
        mockMvc.perform(get("/api/leads")
                        .header("Authorization", jwt)
                        .param("size", "101"))
                .andExpect(status().isBadRequest());

        // ==========================================
        // 5. Test Idempotency filter on Lead POST
        // ==========================================
        String idempotencyKey = UUID.randomUUID().toString();
        LeadCreateDto leadDto = LeadCreateDto.builder()
                .firstName("Peter")
                .lastName("Parker")
                .email("peter@dailybugle.com")
                .companyName("Daily Bugle")
                .companySize("10-50")
                .leadSource("WEBSITE")
                .build();

        String payload = objectMapper.writeValueAsString(leadDto);

        // First request - executes and stores
        MvcResult firstResult = mockMvc.perform(post("/api/leads")
                        .header("Authorization", jwt)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        String firstResponse = firstResult.getResponse().getContentAsString();

        // Second request - returns replayed response
        MvcResult secondResult = mockMvc.perform(post("/api/leads")
                        .header("Authorization", jwt)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        String secondResponse = secondResult.getResponse().getContentAsString();

        assertThat(firstResponse).isEqualTo(secondResponse);

        // ==========================================
        // 6. Test Soft Deletes (Delete and exclusion)
        // ==========================================
        String leadIdStr = com.jayway.jsonpath.JsonPath.read(firstResponse, "$.data.id");
        UUID leadId = UUID.fromString(leadIdStr);

        // Lead exists
        mockMvc.perform(get("/api/leads/" + leadId)
                        .header("Authorization", jwt))
                .andExpect(status().isOk());

        // Soft delete the lead
        mockMvc.perform(delete("/api/leads/" + leadId)
                        .header("Authorization", jwt))
                .andExpect(status().isNoContent());

        // Lead is now hidden/excluded (returns 404)
        mockMvc.perform(get("/api/leads/" + leadId)
                        .header("Authorization", jwt))
                .andExpect(status().isNotFound());
    }
}

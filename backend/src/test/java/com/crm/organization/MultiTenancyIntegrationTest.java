package com.crm.organization;

import com.crm.identity.api.dto.JwtResponseDto;
import com.crm.identity.api.dto.LoginRequestDto;
import com.crm.identity.api.dto.UserRegistrationDto;
import com.crm.identity.service.AuthService;
import com.crm.lead.api.dto.LeadCreateDto;
import com.crm.organization.api.OrganizationApi;
import com.crm.organization.api.dto.OrganizationCreateDto;
import com.crm.organization.api.dto.OrganizationResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class MultiTenancyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationApi organizationApi;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete Multi-Tenancy Data Isolation Test")
    void verifyDataIsolationBetweenTenants() throws Exception {
        // 1. Create Organization A and Organization B
        // Note: Creating organization requires ROLE_ADMIN. We can create them programmatically using organizationApi.
        OrganizationResponseDto orgA = organizationApi.createOrganization(OrganizationCreateDto.builder()
                .name("Stark Enterprises")
                .subdomain("stark")
                .build());

        OrganizationResponseDto orgB = organizationApi.createOrganization(OrganizationCreateDto.builder()
                .name("Oscorp Industries")
                .subdomain("oscorp")
                .build());

        // 2. Register User A (Org A) and User B (Org B)
        com.crm.infrastructure.tenant.TenantContext.computeInTenantContext(orgA.getId(), () ->
            authService.register(UserRegistrationDto.builder()
                    .email("tony@stark.com")
                    .password("ironman123")
                    .firstName("Tony")
                    .lastName("Stark")
                    .roleName("ROLE_ADMIN")
                    .organizationId(orgA.getId())
                    .build())
        );

        com.crm.infrastructure.tenant.TenantContext.computeInTenantContext(orgB.getId(), () ->
            authService.register(UserRegistrationDto.builder()
                    .email("norman@oscorp.com")
                    .password("goblin123")
                    .firstName("Norman")
                    .lastName("Osborn")
                    .roleName("ROLE_ADMIN")
                    .organizationId(orgB.getId())
                    .build())
        );

        // 3. Authenticate User A and User B to obtain JWTs
        JwtResponseDto loginA = authService.login(LoginRequestDto.builder()
                .email("tony@stark.com")
                .password("ironman123")
                .build());

        JwtResponseDto loginB = authService.login(LoginRequestDto.builder()
                .email("norman@oscorp.com")
                .password("goblin123")
                .build());

        String tokenA = "Bearer " + loginA.getToken();
        String tokenB = "Bearer " + loginB.getToken();

        // 4. Create a Lead in Tenant A (using User A token)
        String leadPayloadA = objectMapper.writeValueAsString(LeadCreateDto.builder()
                .firstName("Peter")
                .lastName("Parker")
                .email("peter.parker@dailybugle.com")
                .companyName("Daily Bugle")
                .companySize("10-50")
                .leadSource("WEBSITE")
                .build());

        String leadResponseString = mockMvc.perform(post("/api/leads")
                        .header("Authorization", tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leadPayloadA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.firstName").value("Peter"))
                .andReturn().getResponse().getContentAsString();

        String leadIdStr = com.jayway.jsonpath.JsonPath.read(leadResponseString, "$.data.id");
        UUID createdLeadAId = UUID.fromString(leadIdStr);

        // 5. Query Leads with User A token -> should find the lead
        mockMvc.perform(get("/api/leads")
                        .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Peter"));

        // 6. Query Leads with User B token -> should NOT see the lead from Tenant A
        mockMvc.perform(get("/api/leads")
                        .header("Authorization", tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        // 7. Attempt to query Tenant A's lead by ID with User B token -> should return 404 (ResourceNotFoundException)
        mockMvc.perform(get("/api/leads/" + createdLeadAId)
                        .header("Authorization", tokenB))
                .andExpect(status().isNotFound());
    }
}

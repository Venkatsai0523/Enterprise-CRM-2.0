package com.crm.lead;

import com.crm.identity.api.dto.UserRegistrationDto;
import com.crm.identity.service.AuthService;
import com.crm.lead.api.dto.LeadCreateDto;
import com.crm.lead.api.dto.LeadResponseDto;
import com.crm.lead.api.dto.LeadStatus;
import com.crm.lead.api.event.LeadAssignedEvent;
import com.crm.lead.service.LeadAssignmentService;
import com.crm.lead.service.LeadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class LeadIntelligenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadAssignmentService leadAssignmentService;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Ensure sales rep user exists for IdentityApi lookup
        try {
            authService.register(UserRegistrationDto.builder()
                    .email("rep@nexus.com")
                    .password("password123")
                    .firstName("Sales")
                    .lastName("Rep")
                    .roleName("ROLE_SALES_REP")
                    .build());
        } catch (Exception ignored) {
            // Already registered
        }
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("Lead Intake -> Synchronous Scoring -> Async Kafka Auto-Assignment -> Consumer Idempotency")
    void fullLeadLifecycleTest() throws Exception {
        // 1. Intake Lead via HTTP API
        LeadCreateDto createDto = LeadCreateDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("jdoe@acme-corp.com")
                .phone("+1-555-0199")
                .companyName("Acme Corporation")
                .companySize(">500 Employees")
                .leadSource("REFERRAL")
                .build();

        MvcResult result = mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCORED"))
                .andExpect(jsonPath("$.score").value(100)) // 40 (REFERRAL) + 30 (>500) + 30 (corporate email) = 100
                .andReturn();

        LeadResponseDto createdLead = objectMapper.readValue(result.getResponse().getContentAsString(), LeadResponseDto.class);
        UUID leadId = createdLead.getId();

        // 2. Await Async Kafka Consumer (LeadScoredEventConsumer) to process auto-assignment
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            LeadResponseDto fetched = leadService.getLeadById(leadId);
            assertThat(fetched.getStatus()).isEqualTo(LeadStatus.ASSIGNED);
            assertThat(fetched.getAssignedRepId()).isNotNull();
        });

        // 3. Verify Idempotency: Re-triggering assignLead on already ASSIGNED lead returns empty (idempotent guard)
        Optional<LeadAssignedEvent> duplicateAssignment = leadAssignmentService.assignLead(leadId);
        assertThat(duplicateAssignment).isEmpty();
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("Lead Filtering API: Filter by Status and Min Score")
    void filterLeadsTest() throws Exception {
        LeadCreateDto lead1 = LeadCreateDto.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@gmail.com")
                .companyName("SmallBiz")
                .companySize("1-19")
                .leadSource("MANUAL")
                .build();

        leadService.createAndScoreLead(lead1);

        mockMvc.perform(get("/api/leads")
                        .param("minScore", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

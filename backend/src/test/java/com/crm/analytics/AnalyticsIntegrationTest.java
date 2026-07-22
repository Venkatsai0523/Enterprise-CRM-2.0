package com.crm.analytics;

import com.crm.analytics.api.dto.DashboardResponseDto;
import com.crm.identity.api.dto.JwtResponseDto;
import com.crm.identity.api.dto.LoginRequestDto;
import com.crm.identity.api.dto.UserRegistrationDto;
import com.crm.identity.service.AuthService;
import com.crm.lead.api.dto.LeadCreateDto;
import com.crm.lead.service.LeadService;
import com.crm.opportunity.api.dto.OpportunityCreateDto;
import com.crm.opportunity.api.dto.OpportunityStage;
import com.crm.opportunity.api.dto.OpportunityStageUpdateDto;
import com.crm.opportunity.service.OpportunityService;
import com.crm.organization.api.OrganizationApi;
import com.crm.organization.api.dto.OrganizationCreateDto;
import com.crm.organization.api.dto.OrganizationResponseDto;
import com.crm.task.api.dto.TaskCreateDto;
import com.crm.task.api.dto.TaskPriority;
import com.crm.task.api.dto.TaskType;
import com.crm.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationApi organizationApi;

    @Autowired
    private AuthService authService;

    @Autowired
    private LeadService leadService;

    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Analytics Dashboard Calculations & Redis Caching Test")
    void testDashboardAnalyticsAndCaching() throws Exception {
        // 1. Create Organization & User Context
        OrganizationResponseDto org = organizationApi.createOrganization(OrganizationCreateDto.builder()
                .name("Analytics Corp")
                .subdomain("analytics")
                .build());

        com.crm.infrastructure.tenant.TenantContext.computeInTenantContext(org.getId(), () ->
            authService.register(UserRegistrationDto.builder()
                    .email("manager@analytics.com")
                    .password("managerpass")
                    .firstName("Manager")
                    .lastName("Analytics")
                    .roleName("ROLE_MANAGER")
                    .organizationId(org.getId())
                    .build())
        );

        JwtResponseDto login = authService.login(LoginRequestDto.builder()
                .email("manager@analytics.com")
                .password("managerpass")
                .build());

        String token = "Bearer " + login.getToken();
        UUID repId = login.getId();

        // Ensure Redis cache is empty for this tenant before test starts
        String cacheKey = "tenant::" + org.getId() + "::dashboard";
        redisTemplate.delete(cacheKey);

        // 2. Seed Mock Data within the dynamic tenant context
        com.crm.infrastructure.tenant.TenantContext.computeInTenantContext(org.getId(), () -> {
            try {
                // Seed 2 Leads (one converted, one scored but not converted)
                var lead1 = leadService.createAndScoreLead(LeadCreateDto.builder()
                        .firstName("LeBron")
                        .lastName("James")
                        .email("lebron@lakers.com")
                        .companyName("Lakers")
                        .companySize(">500")
                        .leadSource("WEBSITE")
                        .build());
                // Mark lead1 as CONVERTED (status change)
                leadService.updateStatus(lead1.getId(), "CONVERTED");

                leadService.createAndScoreLead(LeadCreateDto.builder()
                        .firstName("Stephen")
                        .lastName("Curry")
                        .email("steph@warriors.com")
                        .companyName("Warriors")
                        .companySize("10-50")
                        .leadSource("REFERRAL")
                        .build());

                // Seed Opportunities (lead1 is converted, create opportunities for it)
                var opp1 = opportunityService.createOpportunity(OpportunityCreateDto.builder()
                        .title("Lakers Jersey Contract")
                        .leadId(lead1.getId())
                        .estimatedValue(new BigDecimal("10000.00"))
                        .build());
                // Transition opp1 to WON stage (using valid sequential updates)
                opportunityService.updateStage(opp1.getId(), OpportunityStageUpdateDto.builder()
                        .stage(OpportunityStage.PROPOSAL)
                        .build());
                opportunityService.updateStage(opp1.getId(), OpportunityStageUpdateDto.builder()
                        .stage(OpportunityStage.NEGOTIATION)
                        .build());
                opportunityService.updateStage(opp1.getId(), OpportunityStageUpdateDto.builder()
                        .stage(OpportunityStage.WON)
                        .build());

                opportunityService.createOpportunity(OpportunityCreateDto.builder()
                        .title("Lakers Arena Sponsorship")
                        .leadId(lead1.getId())
                        .estimatedValue(new BigDecimal("50000.00"))
                        .build());
                // opp2 remains in PROSPECTING stage (estimatedValue = 50,000)

                // Seed Tasks (One Open, One Overdue)
                taskService.createTask(TaskCreateDto.builder()
                        .title("Review Contract Draft")
                        .dueDate(Instant.now().plus(2, ChronoUnit.DAYS))
                        .priority(TaskPriority.MEDIUM)
                        .type(TaskType.TASK)
                        .assignedTo(repId)
                        .relatedToType("LEAD")
                        .relatedToId(lead1.getId())
                        .build());

                taskService.createTask(TaskCreateDto.builder()
                        .title("Overdue Pitch Deck Review")
                        .dueDate(Instant.now().minus(2, ChronoUnit.DAYS)) // Overdue task
                        .priority(TaskPriority.HIGH)
                        .type(TaskType.TASK)
                        .assignedTo(repId)
                        .relatedToType("LEAD")
                        .relatedToId(lead1.getId())
                        .build());

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return null;
        });

        // 3. Request Dashboard Analytics API (Cache Miss -> Compile -> Cache hit next)
        mockMvc.perform(get("/api/analytics/dashboard")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLeads").value(2))
                .andExpect(jsonPath("$.leadConversionRate").value(50.0)) // 1 converted of 2 leads
                .andExpect(jsonPath("$.totalPipelineValue").value(60000.00)) // opp1 (10000) + opp2 (50000)
                .andExpect(jsonPath("$.activePipelineValue").value(50000.00)) // opp2 (50000) is active, opp1 (10000) is WON
                .andExpect(jsonPath("$.dealWinRate").value(100.0)) // 1 won deal of 1 closed deal
                .andExpect(jsonPath("$.openTasks").value(2)) // 2 tasks
                .andExpect(jsonPath("$.overdueTasks").value(1)); // 1 overdue task

        // Verify that the dashboard response was cached in Redis
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        assertThat(cachedValue).isNotNull();
        DashboardResponseDto cachedDto = objectMapper.readValue(cachedValue, DashboardResponseDto.class);
        assertThat(cachedDto.getTotalLeads()).isEqualTo(2);

        // 4. Request Dashboard Analytics again (Should serve from Cache)
        // We will modify the cached value in Redis directly to prove it's read from the cache!
        cachedDto.setTotalLeads(99);
        redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(cachedDto));

        mockMvc.perform(get("/api/analytics/dashboard")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLeads").value(99)) // Serves the mocked value from Redis!
                .andExpect(jsonPath("$.totalPipelineValue").value(60000.00));
    }
}

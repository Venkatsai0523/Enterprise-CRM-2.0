package com.crm.workflow;

import com.crm.identity.api.dto.JwtResponseDto;
import com.crm.identity.api.dto.LoginRequestDto;
import com.crm.identity.api.dto.UserRegistrationDto;
import com.crm.identity.service.AuthService;
import com.crm.notification.api.dto.NotificationResponseDto;
import com.crm.notification.service.NotificationService;
import com.crm.organization.api.OrganizationApi;
import com.crm.organization.api.dto.OrganizationCreateDto;
import com.crm.organization.api.dto.OrganizationResponseDto;
import com.crm.task.api.TaskApi;
import com.crm.task.api.dto.TaskResponseDto;
import com.crm.workflow.api.WorkflowApi;
import com.crm.workflow.api.dto.WorkflowRuleCreateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class WorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationApi organizationApi;

    @Autowired
    private AuthService authService;

    @Autowired
    private WorkflowApi workflowApi;

    @Autowired
    private TaskApi taskApi;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.crm.lead.service.LeadService leadService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Workflow Rules Engine E2E Execution Test")
    void testWorkflowRulesEngineExecution() throws Exception {
        // 1. Create Organization & Admin User — register creates the org automatically
        authService.register(UserRegistrationDto.builder()
                .email("admin@workflow.com")
                .password("adminpass")
                .firstName("Admin")
                .lastName("Workflow")
                .roleName("ROLE_ADMIN")
                .organizationName("Workflow Inc")
                .subdomain("workflow")
                .build());

        OrganizationResponseDto org = organizationApi.findOrganizationBySubdomain("workflow")
                .orElseThrow(() -> new RuntimeException("Org not found"));

        JwtResponseDto login = authService.login(LoginRequestDto.builder()
                .email("admin@workflow.com")
                .password("adminpass")
                .build());

        String token = "Bearer " + login.getToken();

        // Create a real Lead in the database
        com.crm.lead.api.dto.LeadResponseDto lead = com.crm.infrastructure.tenant.TenantContext.computeInTenantContext(org.getId(), () -> 
            leadService.createAndScoreLead(com.crm.lead.api.dto.LeadCreateDto.builder()
                    .firstName("Elon")
                    .lastName("Musk")
                    .email("elon@spacex.com")
                    .companyName("SpaceX")
                    .companySize(">500")
                    .leadSource("WEBSITE")
                    .build())
        );

        // 2. Define a high-value lead automated rule using REST API
        String conditionsJson = "[{\"field\": \"score\", \"operator\": \"GREATER_THAN\", \"value\": \"80\"}]";
        String actionsJson = "[" +
                "{\"type\": \"CREATE_TASK\", \"parameters\": {\"title\": \"High Score Follow-up: {companyName}\", \"daysToDue\": \"2\"}}," +
                "{\"type\": \"SEND_NOTIFICATION\", \"parameters\": {\"messageTemplate\": \"Attention: high value lead scored {score}. Please follow up!\"}}" +
                "]";

        WorkflowRuleCreateDto ruleDto = WorkflowRuleCreateDto.builder()
                .name("High Value Lead Alert")
                .description("Alert rep and create task for lead scores > 80")
                .triggerEvent("LEAD_SCORED")
                .conditionsJson(conditionsJson)
                .actionsJson(actionsJson)
                .build();

        mockMvc.perform(post("/api/workflows/rules")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("High Value Lead Alert"));

        // 3. Simulate Lead Scoring Event and check rules engine evaluation (direct API call)
        UUID leadId = lead.getId();
        UUID repId = login.getId(); // Admin user is also the Sales Rep in this test context

        Map<String, Object> leadContext = new HashMap<>();
        leadContext.put("entityId", leadId);
        leadContext.put("entityType", "LEAD");
        leadContext.put("email", "elon@spacex.com");
        leadContext.put("companyName", "SpaceX");
        leadContext.put("score", 95);
        leadContext.put("assignedRepId", repId);

        // Run evaluateAndExecute synchronously inside the tenant context
        com.crm.infrastructure.tenant.TenantContext.computeInTenantContext(org.getId(), () -> {
            workflowApi.evaluateAndExecute("LEAD_SCORED", leadContext, org.getId());
            return null;
        });

        // 4. Verify Task was created successfully for this lead
        // The task is linked to the lead (relatedToType = "LEAD", relatedToId = leadId)
        // Note: Running in tenant context
        com.crm.infrastructure.tenant.TenantContext.computeInTenantContext(org.getId(), () -> {
            List<TaskResponseDto> tasks = taskApi.findTasksByRelatedObject("LEAD", leadId);
            assertThat(tasks).isNotEmpty();
            assertThat(tasks.get(0).getTitle()).isEqualTo("High Score Follow-up: SpaceX");
            assertThat(tasks.get(0).getAssignedTo()).isEqualTo(repId);
            return null;
        });

        // 5. Verify Notification was sent successfully to the representative
        com.crm.infrastructure.tenant.TenantContext.computeInTenantContext(org.getId(), () -> {
            Page<NotificationResponseDto> notifications = notificationService.getNotificationsForRecipient(repId, 0, 10);
            assertThat(notifications.getContent()).isNotEmpty();
            
            boolean foundWorkflowNotification = false;
            for (NotificationResponseDto n : notifications.getContent()) {
                if (n.getType().equals("WORKFLOW_ALERT") && n.getMessage().contains("high value lead scored 95")) {
                    foundWorkflowNotification = true;
                    break;
                }
            }
            assertThat(foundWorkflowNotification).isTrue();
            return null;
        });
    }
}

package com.crm.task;

import com.crm.customer.api.dto.Customer360ResponseDto;
import com.crm.customer.api.dto.CustomerAccountResponseDto;
import com.crm.customer.service.CustomerService;
import com.crm.identity.api.dto.UserRegistrationDto;
import com.crm.identity.api.dto.UserResponseDto;
import com.crm.identity.service.AuthService;
import com.crm.lead.api.dto.LeadCreateDto;
import com.crm.lead.api.dto.LeadResponseDto;
import com.crm.lead.service.LeadService;
import com.crm.notification.api.dto.NotificationResponseDto;
import com.crm.notification.service.NotificationService;
import com.crm.opportunity.api.dto.OpportunityCreateDto;
import com.crm.opportunity.api.dto.OpportunityResponseDto;
import com.crm.opportunity.api.dto.OpportunityStage;
import com.crm.opportunity.api.dto.OpportunityStageUpdateDto;
import com.crm.opportunity.service.OpportunityService;
import com.crm.task.api.dto.*;
import com.crm.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LeadService leadService;

    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private NotificationService notificationService;

    private UUID repId;
    private UUID leadId;

    @BeforeEach
    void setUp() {
        // Register a sales rep user
        String email = "rep-" + UUID.randomUUID() + "@nexus.com";
        try {
            UserResponseDto user = authService.register(UserRegistrationDto.builder()
                    .email(email)
                    .password("password123")
                    .firstName("Task")
                    .lastName("Rep")
                    .roleName("ROLE_SALES_REP")
                    .build());
            repId = user.getId();
        } catch (Exception ignored) {
            // Rep already registered
        }

        // Create a lead
        LeadResponseDto lead = leadService.createAndScoreLead(LeadCreateDto.builder()
                .firstName("John")
                .lastName("Task")
                .email("jtask-" + UUID.randomUUID() + "@cyberdyne.com")
                .companyName("Cyberdyne Systems")
                .companySize(">500")
                .leadSource("WEBSITE")
                .build());
        leadId = lead.getId();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Task Lifecycle: Create -> Assign Notification -> Complete -> Audit -> Customer 360 view")
    void taskLifecycleIntegrationTest() throws Exception {
        // 1. Create a Task (relates to the lead, assigned to the sales rep)
        TaskCreateDto createDto = TaskCreateDto.builder()
                .title("Initial Discovery Call")
                .description("Call to discuss cloud requirements")
                .dueDate(Instant.now().plusSeconds(3600))
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .type(TaskType.CALL)
                .assignedTo(repId)
                .relatedToType("LEAD")
                .relatedToId(leadId)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Initial Discovery Call"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.type").value("CALL"))
                .andExpect(jsonPath("$.assignedTo").value(repId.toString()))
                .andReturn();

        TaskResponseDto task = objectMapper.readValue(createResult.getResponse().getContentAsString(), TaskResponseDto.class);
        UUID taskId = task.getId();

        // 2. Await Async notification generation (TaskAssignedEvent Consumer)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Page<NotificationResponseDto> notifications = notificationService.getNotificationsForRecipient(repId, 0, 10);
            assertThat(notifications.getContent()).isNotEmpty();
            assertThat(notifications.getContent().stream()
                    .anyMatch(n -> n.getType().equals("TASK_ASSIGNED") && n.getMessage().contains("Initial Discovery Call")))
                    .isTrue();
        });

        // 3. Complete the task using stage status update
        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 4. Verify that Audit Log is recorded for the task status change
        mockMvc.perform(get("/api/audit-logs")
                        .param("entityName", "TASK")
                        .param("entityId", taskId.toString())
                        .param("action", "STATUS_CHANGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].oldState").value("TODO"))
                .andExpect(jsonPath("$.content[0].newState").value("COMPLETED"));

        // 5. Test Customer 360 view integration
        // Convert the lead into an opportunity and win it to generate a customer account
        OpportunityResponseDto opp = opportunityService.createOpportunity(OpportunityCreateDto.builder()
                .title("batman-deal")
                .leadId(leadId)
                .estimatedValue(new BigDecimal("12000.00"))
                .build());

        opportunityService.updateStage(opp.getId(), OpportunityStageUpdateDto.builder()
                .stage(OpportunityStage.PROPOSAL)
                .build());

        opportunityService.updateStage(opp.getId(), OpportunityStageUpdateDto.builder()
                .stage(OpportunityStage.NEGOTIATION)
                .build());

        opportunityService.updateStage(opp.getId(), OpportunityStageUpdateDto.builder()
                .stage(OpportunityStage.WON)
                .build());

        // Await background customer conversion
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            Page<CustomerAccountResponseDto> customers = customerService.getCustomerAccounts(0, 10);
            return customers.getContent().stream().anyMatch(c -> c.getDomainName().contains("cyberdyne.com"));
        });

        CustomerAccountResponseDto customer = customerService.getCustomerAccounts(0, 10).getContent().stream()
                .filter(c -> c.getDomainName().contains("cyberdyne.com"))
                .findFirst().orElseThrow();

        // Create a task associated with this won opportunity
        TaskCreateDto taskForOpp = TaskCreateDto.builder()
                .title("Follow-up meeting after signing")
                .assignedTo(repId)
                .relatedToType("OPPORTUNITY")
                .relatedToId(opp.getId())
                .build();

        taskService.createTask(taskForOpp);

        // Fetch Customer 360 profile and verify activity history is populated
        MvcResult customer360Result = mockMvc.perform(get("/api/customers/" + customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activities").isArray())
                .andReturn();

        Customer360ResponseDto customer360 = objectMapper.readValue(customer360Result.getResponse().getContentAsString(), Customer360ResponseDto.class);
        assertThat(customer360.getActivities()).isNotEmpty();
        assertThat(customer360.getActivities().get(0).getTitle()).isEqualTo("Follow-up meeting after signing");
    }
}

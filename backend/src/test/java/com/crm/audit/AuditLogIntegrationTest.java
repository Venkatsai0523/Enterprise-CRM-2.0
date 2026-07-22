package com.crm.audit;

import com.crm.lead.api.dto.LeadCreateDto;
import com.crm.lead.api.dto.LeadResponseDto;
import com.crm.lead.service.LeadService;
import com.crm.opportunity.api.dto.OpportunityCreateDto;
import com.crm.opportunity.api.dto.OpportunityResponseDto;
import com.crm.opportunity.api.dto.OpportunityStage;
import com.crm.opportunity.api.dto.OpportunityStageUpdateDto;
import com.crm.opportunity.service.OpportunityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private OpportunityService opportunityService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("State Transitions -> Audit Log Persistence -> Administrative Query API")
    void auditLogRecordingAndQueryTest() throws Exception {
        // 1. Create Lead (Triggers LEAD_CREATED audit log)
        LeadResponseDto lead = leadService.createAndScoreLead(LeadCreateDto.builder()
                .firstName("Bruce")
                .lastName("Wayne")
                .email("bruce@wayne.com")
                .companyName("Wayne Enterprises")
                .companySize(">500")
                .leadSource("WEBSITE")
                .build());

        // 2. Create Opportunity & Update Stage (Triggers OPPORTUNITY_CREATED & STAGE_CHANGE audit logs)
        OpportunityResponseDto opp = opportunityService.createOpportunity(OpportunityCreateDto.builder()
                .title("Batmobile Defense Fleet")
                .leadId(lead.getId())
                .estimatedValue(new BigDecimal("250000.00"))
                .build());

        opportunityService.updateStage(opp.getId(), OpportunityStageUpdateDto.builder()
                .stage(OpportunityStage.PROPOSAL)
                .build());

        // 3. Query GET /api/audit-logs?entityName=LEAD
        mockMvc.perform(get("/api/audit-logs")
                        .param("entityName", "LEAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].entityName").value("LEAD"));

        // 4. Query GET /api/audit-logs?entityName=OPPORTUNITY
        mockMvc.perform(get("/api/audit-logs")
                        .param("entityName", "OPPORTUNITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].entityName").value("OPPORTUNITY"));
    }
}

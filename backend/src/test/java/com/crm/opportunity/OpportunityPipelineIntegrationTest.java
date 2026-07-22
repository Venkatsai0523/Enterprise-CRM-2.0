package com.crm.opportunity;

import com.crm.lead.api.dto.LeadCreateDto;
import com.crm.lead.api.dto.LeadResponseDto;
import com.crm.lead.service.LeadService;
import com.crm.opportunity.api.dto.*;
import com.crm.opportunity.service.OpportunityService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("null")
class OpportunityPipelineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LeadService leadService;

    @Autowired
    private OpportunityService opportunityService;

    private UUID leadId;

    @BeforeEach
    void setUp() {
        LeadResponseDto lead = leadService.createAndScoreLead(LeadCreateDto.builder()
                .firstName("Sarah")
                .lastName("Connor")
                .email("sarah@cyberdyne.com")
                .companyName("Cyberdyne")
                .companySize(">500")
                .leadSource("WEBSITE")
                .build());
        leadId = lead.getId();
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("Full Opportunity Pipeline: Create -> Stage Transitions -> Win Deal")
    void opportunityPipelineLifecycleTest() throws Exception {
        // 1. Create Opportunity
        OpportunityCreateDto createDto = OpportunityCreateDto.builder()
                .title("Enterprise CRM License Deal")
                .leadId(leadId)
                .estimatedValue(new BigDecimal("50000.00"))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/opportunities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stage").value("PROSPECTING"))
                .andExpect(jsonPath("$.data.estimatedValue").value(50000.00))
                .andReturn();

        String responseString = createResult.getResponse().getContentAsString();
        String oppIdStr = com.jayway.jsonpath.JsonPath.read(responseString, "$.data.id");
        UUID oppId = UUID.fromString(oppIdStr);

        // 2. Transition PROSPECTING -> PROPOSAL
        mockMvc.perform(patch("/api/opportunities/" + oppId + "/stage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OpportunityStageUpdateDto.builder()
                                .stage(OpportunityStage.PROPOSAL)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stage").value("PROPOSAL"));

        // 3. Transition PROPOSAL -> NEGOTIATION
        mockMvc.perform(patch("/api/opportunities/" + oppId + "/stage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OpportunityStageUpdateDto.builder()
                                .stage(OpportunityStage.NEGOTIATION)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stage").value("NEGOTIATION"));

        // 4. Transition NEGOTIATION -> WON
        mockMvc.perform(patch("/api/opportunities/" + oppId + "/stage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OpportunityStageUpdateDto.builder()
                                .stage(OpportunityStage.WON)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stage").value("WON"))
                .andExpect(jsonPath("$.data.closedAt").exists());

        // 5. Attempt Invalid Transition: WON -> PROSPECTING -> HTTP 400 Bad Request
        mockMvc.perform(patch("/api/opportunities/" + oppId + "/stage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OpportunityStageUpdateDto.builder()
                                .stage(OpportunityStage.PROSPECTING)
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Lost Deal & Aggregated Lost Analysis Analytics")
    void lostAnalysisTest() throws Exception {
        OpportunityCreateDto oppDto = OpportunityCreateDto.builder()
                .title("Cloud Infrastructure Expansion")
                .leadId(leadId)
                .estimatedValue(new BigDecimal("25000.00"))
                .build();

        OpportunityResponseDto opp = opportunityService.createOpportunity(oppDto);

        // Mark as LOST
        opportunityService.updateStage(opp.getId(), OpportunityStageUpdateDto.builder()
                .stage(OpportunityStage.LOST)
                .lostReason("Competitor won on pricing")
                .build());

        // Query Lost Analysis endpoint
        MvcResult result = mockMvc.perform(get("/api/opportunities/analytics/lost-analysis"))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        String dataJson = objectMapper.writeValueAsString(com.jayway.jsonpath.JsonPath.read(responseString, "$.data"));
        List<LostAnalysisDto> lostReport = objectMapper.readValue(
                dataJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, LostAnalysisDto.class)
        );

        assertThat(lostReport).isNotEmpty();
        assertThat(lostReport.stream().anyMatch(item -> "Competitor won on pricing".equals(item.getLostReason()))).isTrue();
    }
}

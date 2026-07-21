package com.crm.customer;

import com.crm.customer.api.dto.Customer360ResponseDto;
import com.crm.customer.api.dto.CustomerAccountResponseDto;
import com.crm.customer.service.CustomerService;
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
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerConversionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private CustomerService customerService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deduplication & Conversion: Two won deals with same domain merge into 1 Customer Account")
    void domainDeduplicationTest() throws Exception {
        // 1. Create Lead 1 & Opportunity 1 (stark-industries.com)
        LeadResponseDto lead1 = leadService.createAndScoreLead(LeadCreateDto.builder()
                .firstName("Tony")
                .lastName("Stark")
                .email("tony@stark-industries.com")
                .companyName("Stark Industries")
                .companySize(">500")
                .leadSource("WEBSITE")
                .build());

        OpportunityResponseDto opp1 = opportunityService.createOpportunity(OpportunityCreateDto.builder()
                .title("Arc Reactor Defense Contract")
                .leadId(lead1.getId())
                .estimatedValue(new BigDecimal("100000.00"))
                .build());

        // 2. Transition Opportunity 1: PROSPECTING -> PROPOSAL -> NEGOTIATION -> WON -> triggers DealWonEvent -> Kafka -> DealWonConsumer
        opportunityService.updateStage(opp1.getId(), OpportunityStageUpdateDto.builder().stage(OpportunityStage.PROPOSAL).build());
        opportunityService.updateStage(opp1.getId(), OpportunityStageUpdateDto.builder().stage(OpportunityStage.NEGOTIATION).build());
        opportunityService.updateStage(opp1.getId(), OpportunityStageUpdateDto.builder().stage(OpportunityStage.WON).build());

        // 3. Await Kafka Consumer to create Account 1
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Page<CustomerAccountResponseDto> accounts = customerService.getCustomerAccounts(0, 10);
            assertThat(accounts.getContent().stream().anyMatch(a -> "stark-industries.com".equals(a.getDomainName()))).isTrue();
        });

        CustomerAccountResponseDto account = customerService.getCustomerAccounts(0, 10).getContent().stream()
                .filter(a -> "stark-industries.com".equals(a.getDomainName()))
                .findFirst().orElseThrow();
        UUID accountId = account.getId();

        // 4. Create Lead 2 & Opportunity 2 under SAME domain (stark-industries.com)
        LeadResponseDto lead2 = leadService.createAndScoreLead(LeadCreateDto.builder()
                .firstName("Pepper")
                .lastName("Potts")
                .email("pepper@stark-industries.com") // Same email domain!
                .companyName("Stark Industries")
                .companySize(">500")
                .leadSource("REFERRAL")
                .build());

        OpportunityResponseDto opp2 = opportunityService.createOpportunity(OpportunityCreateDto.builder()
                .title("Clean Energy Expansion")
                .leadId(lead2.getId())
                .estimatedValue(new BigDecimal("75000.00"))
                .build());

        // 5. Transition Opportunity 2: PROSPECTING -> PROPOSAL -> NEGOTIATION -> WON
        opportunityService.updateStage(opp2.getId(), OpportunityStageUpdateDto.builder().stage(OpportunityStage.PROPOSAL).build());
        opportunityService.updateStage(opp2.getId(), OpportunityStageUpdateDto.builder().stage(OpportunityStage.NEGOTIATION).build());
        opportunityService.updateStage(opp2.getId(), OpportunityStageUpdateDto.builder().stage(OpportunityStage.WON).build());

        // 6. Await Kafka Consumer -> Deduplication should merge Opp 2 into existing Account 1 (not create new account)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Customer360ResponseDto c360 = customerService.getCustomer360(accountId);
            assertThat(c360.getOpportunityCount()).isEqualTo(2);
            assertThat(c360.getTotalLifetimeValue()).isEqualByComparingTo("175000.00");
        });

        // 7. Verify HTTP GET /api/customers/{id} 360 View
        mockMvc.perform(get("/api/customers/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domainName").value("stark-industries.com"))
                .andExpect(jsonPath("$.opportunityCount").value(2))
                .andExpect(jsonPath("$.totalLifetimeValue").value(175000.00));
    }
}

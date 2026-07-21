package com.crm.lead;

import com.crm.lead.entity.Lead;
import com.crm.lead.service.LeadScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeadScoringServiceTest {

    private LeadScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new LeadScoringService();
    }

    @Test
    @DisplayName("Scoring Engine: Maximum score scenario (Referral + Enterprise >500 + Corporate domain)")
    void maxScoreScenario() {
        Lead lead = Lead.builder()
                .leadSource("REFERRAL")
                .companySize(">500")
                .email("john@enterprise-corp.com")
                .build();

        int score = scoringService.calculateScore(lead);
        assertThat(score).isEqualTo(100); // 40 (REFERRAL) + 30 (>500) + 30 (Corporate) = 100
    }

    @Test
    @DisplayName("Scoring Engine: Mid score scenario (Website + 100-500 + Corporate domain)")
    void midScoreScenario() {
        Lead lead = Lead.builder()
                .leadSource("WEBSITE")
                .companySize("100-500")
                .email("alice@midsize.com")
                .build();

        int score = scoringService.calculateScore(lead);
        assertThat(score).isEqualTo(80); // 30 (WEBSITE) + 20 (100-500) + 30 (Corporate) = 80
    }

    @Test
    @DisplayName("Scoring Engine: Personal email domain scenario (gmail/yahoo)")
    void personalEmailScenario() {
        Lead lead = Lead.builder()
                .leadSource("CAMPAIGN")
                .companySize("1-19")
                .email("bob@gmail.com")
                .build();

        int score = scoringService.calculateScore(lead);
        assertThat(score).isEqualTo(15); // 10 (CAMPAIGN) + 5 (1-19) + 0 (gmail) = 15
    }
}

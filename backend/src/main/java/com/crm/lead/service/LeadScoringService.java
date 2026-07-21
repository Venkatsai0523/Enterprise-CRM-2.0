package com.crm.lead.service;

import com.crm.lead.entity.Lead;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Synchronous scoring service applying weighted rules engine to calculate a 0-100 score.
 */
@Service
public class LeadScoringService {

    private static final Set<String> FREE_EMAIL_PROVIDERS = Set.of(
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "icloud.com"
    );

    public int calculateScore(Lead lead) {
        int score = 0;

        // 1. Source Quality Weighted Points
        if (lead.getLeadSource() != null) {
            String source = lead.getLeadSource().toUpperCase();
            score += switch (source) {
                case "REFERRAL" -> 40;
                case "PARTNER" -> 35;
                case "WEBSITE" -> 30;
                case "MANUAL" -> 15;
                default -> 10;
            };
        }

        // 2. Company Size Weighted Points
        if (lead.getCompanySize() != null) {
            String size = lead.getCompanySize().trim().toUpperCase();
            if (size.contains(">500") || size.contains("ENTERPRISE") || size.contains("1000")) {
                score += 30;
            } else if (size.contains("100-500") || size.contains("MIDMARKET") || size.contains("500")) {
                score += 20;
            } else if (size.contains("20-99")) {
                score += 10;
            } else {
                score += 5;
            }
        }

        // 3. Corporate Domain Email Signal
        if (lead.getEmail() != null && lead.getEmail().contains("@")) {
            String domain = lead.getEmail().substring(lead.getEmail().indexOf("@") + 1).toLowerCase();
            if (!FREE_EMAIL_PROVIDERS.contains(domain)) {
                score += 30; // Business corporate email domain bonus
            }
        }

        // Bound final score between 0 and 100
        return Math.min(100, Math.max(0, score));
    }
}

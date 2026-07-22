package com.crm.analytics.service;

import com.crm.analytics.api.dto.DashboardResponseDto;
import com.crm.lead.api.LeadApi;
import com.crm.opportunity.api.OpportunityApi;
import com.crm.opportunity.api.dto.OpportunityResponseDto;
import com.crm.opportunity.api.dto.OpportunityStage;
import com.crm.task.api.TaskApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class AnalyticsService {

    private final LeadApi leadApi;
    private final OpportunityApi opportunityApi;
    private final TaskApi taskApi;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "tenant::";
    private static final String CACHE_KEY_SUFFIX = "::dashboard";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    public DashboardResponseDto getDashboardAnalytics(UUID organizationId) {
        String cacheKey = CACHE_KEY_PREFIX + organizationId.toString() + CACHE_KEY_SUFFIX;
        
        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson != null) {
                log.info("Returning cached dashboard metrics for tenant: {}", organizationId);
                return objectMapper.readValue(cachedJson, DashboardResponseDto.class);
            }
        } catch (Exception ex) {
            log.warn("Failed to retrieve dashboard metrics from Redis cache", ex);
        }

        log.info("Cache miss. Compiling dashboard metrics for tenant: {}", organizationId);
        DashboardResponseDto dashboard = compileDashboard(organizationId);

        try {
            String jsonString = objectMapper.writeValueAsString(dashboard);
            redisTemplate.opsForValue().set(cacheKey, jsonString, CACHE_TTL);
            log.info("Cached dashboard metrics in Redis for tenant: {}", organizationId);
        } catch (Exception ex) {
            log.warn("Failed to write dashboard metrics to Redis cache", ex);
        }

        return dashboard;
    }

    private DashboardResponseDto compileDashboard(UUID organizationId) {
        // 1. Lead Metrics
        long totalLeads = leadApi.countLeads();
        long convertedLeads = leadApi.countLeadsByStatus("CONVERTED");
        double averageScore = leadApi.getAverageLeadScore();
        Map<String, Long> leadsBySource = leadApi.countLeadsBySource();

        double conversionRate = totalLeads > 0 ? ((double) convertedLeads / totalLeads) * 100.0 : 0.0;

        // 2. Opportunity Metrics
        List<OpportunityResponseDto> opportunities = opportunityApi.findAllOpportunities();
        
        BigDecimal totalPipeline = BigDecimal.ZERO;
        BigDecimal activePipeline = BigDecimal.ZERO;
        long wonCount = 0;
        long lostCount = 0;
        
        Map<String, BigDecimal> pipelineByStage = new HashMap<>();
        for (OpportunityStage stage : OpportunityStage.values()) {
            pipelineByStage.put(stage.name(), BigDecimal.ZERO);
        }

        for (OpportunityResponseDto opp : opportunities) {
            BigDecimal val = opp.getEstimatedValue() != null ? opp.getEstimatedValue() : BigDecimal.ZERO;
            totalPipeline = totalPipeline.add(val);
            
            OpportunityStage stage = opp.getStage();
            String stageName = stage != null ? stage.name() : "PROSPECTING";
            pipelineByStage.put(stageName, pipelineByStage.getOrDefault(stageName, BigDecimal.ZERO).add(val));

            if (!"WON".equals(stageName) && !"LOST".equals(stageName)) {
                activePipeline = activePipeline.add(val);
            } else if ("WON".equals(stageName)) {
                wonCount++;
            } else if ("LOST".equals(stageName)) {
                lostCount++;
            }
        }

        long closedDeals = wonCount + lostCount;
        double winRate = closedDeals > 0 ? ((double) wonCount / closedDeals) * 100.0 : 0.0;

        // 3. Task Metrics
        long openTasks = taskApi.countTasksByStatus("TODO") + taskApi.countTasksByStatus("IN_PROGRESS");
        long overdueTasks = taskApi.countOverdueTasks();

        return DashboardResponseDto.builder()
                .totalLeads(totalLeads)
                .leadConversionRate(conversionRate)
                .averageLeadScore(averageScore)
                .leadsBySource(leadsBySource)
                .totalPipelineValue(totalPipeline)
                .activePipelineValue(activePipeline)
                .dealWinRate(winRate)
                .pipelineByStage(pipelineByStage)
                .openTasks(openTasks)
                .overdueTasks(overdueTasks)
                .build();
    }
}

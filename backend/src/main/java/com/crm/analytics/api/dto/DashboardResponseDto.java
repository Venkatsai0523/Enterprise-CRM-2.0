package com.crm.analytics.api.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private long totalLeads;
    private double leadConversionRate;
    private double averageLeadScore;
    private Map<String, Long> leadsBySource;
    
    private BigDecimal totalPipelineValue;
    private BigDecimal activePipelineValue;
    private double dealWinRate;
    private Map<String, BigDecimal> pipelineByStage;
    
    private long openTasks;
    private long overdueTasks;
}

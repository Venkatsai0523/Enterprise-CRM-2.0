package com.crm.analytics.controller;

import com.crm.analytics.api.dto.DashboardResponseDto;
import com.crm.analytics.service.AnalyticsService;
import com.crm.common.exception.BadRequestException;
import com.crm.infrastructure.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Reporting", description = "Cross-entity dashboard metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get dashboard analytics", description = "Retrieves aggregate performance metrics across leads, opportunities, and tasks for the caller's organization. Cached in Redis.")
    public ResponseEntity<DashboardResponseDto> getDashboardAnalytics() {
        UUID organizationId = TenantContext.getTenantId();
        if (organizationId == null) {
            throw new BadRequestException("Tenant context not resolved.");
        }

        DashboardResponseDto dashboard = analyticsService.getDashboardAnalytics(organizationId);
        return ResponseEntity.ok(dashboard);
    }
}

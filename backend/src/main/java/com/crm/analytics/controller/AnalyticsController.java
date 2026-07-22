package com.crm.analytics.controller;

import com.crm.analytics.api.dto.DashboardResponseDto;
import com.crm.analytics.service.AnalyticsService;
import com.crm.common.exception.BadRequestException;
import com.crm.infrastructure.tenant.TenantContext;
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
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DashboardResponseDto> getDashboardAnalytics() {
        UUID organizationId = TenantContext.getTenantId();
        if (organizationId == null) {
            throw new BadRequestException("Tenant context not resolved.");
        }

        DashboardResponseDto dashboard = analyticsService.getDashboardAnalytics(organizationId);
        return ResponseEntity.ok(dashboard);
    }
}

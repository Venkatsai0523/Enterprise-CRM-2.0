package com.crm.infrastructure.tenant;

import com.crm.identity.api.dto.CustomUserDetails;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class TenantContextResolver implements CurrentTenantIdentifierResolver<UUID>, HibernatePropertiesCustomizer {

    public static final UUID DEFAULT_ORG_ID = UUID.fromString("e8888888-8888-8888-8888-888888888888");

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        // 1. Check TenantContext (ThreadLocal) first
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return tenantId;
        }

        // 2. Check Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            if (userDetails.getOrganizationId() != null) {
                return userDetails.getOrganizationId();
            }
        }

        // 3. Fallback to default organization
        return DEFAULT_ORG_ID;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}

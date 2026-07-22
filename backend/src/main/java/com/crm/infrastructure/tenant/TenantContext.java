package com.crm.infrastructure.tenant;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static <T> T computeInTenantContext(UUID tenantId, java.util.function.Supplier<T> supplier) {
        UUID previousTenant = getTenantId();
        setTenantId(tenantId);
        try {
            return supplier.get();
        } finally {
            if (previousTenant != null) {
                setTenantId(previousTenant);
            } else {
                clear();
            }
        }
    }
}

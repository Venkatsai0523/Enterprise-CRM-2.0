import axiosInstance from './axios';

export interface AuditLogFilterParams {
  entityName?: string;
  action?: string;
  entityId?: string;
  page?: number;
  size?: number;
}

export const auditService = {
  async getAuditLogs(params: AuditLogFilterParams = {}): Promise<any> {
    return axiosInstance.get('/api/audit-logs', { params });
  },
};

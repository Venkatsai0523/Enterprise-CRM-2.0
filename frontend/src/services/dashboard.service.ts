import axiosInstance from './axios';

export const dashboardService = {
  async getDashboardMetrics(): Promise<any> {
    return axiosInstance.get('/api/analytics/dashboard');
  },
};

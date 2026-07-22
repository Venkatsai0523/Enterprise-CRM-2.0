import axiosInstance from './axios';

export const workflowService = {
  async getWorkflowRules(): Promise<any> {
    return axiosInstance.get('/api/workflows/rules');
  },

  async createWorkflowRule(rule: any): Promise<any> {
    return axiosInstance.post('/api/workflows/rules', rule);
  },

  async updateWorkflowRule(id: string, rule: any): Promise<any> {
    return axiosInstance.put(`/api/workflows/rules/${id}`, rule);
  },

  async getWorkflowRule(id: string): Promise<any> {
    return axiosInstance.get(`/api/workflows/rules/${id}`);
  },
};

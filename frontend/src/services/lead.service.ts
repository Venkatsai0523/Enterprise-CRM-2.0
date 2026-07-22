import axiosInstance from './axios';

export interface LeadFilterParams {
  status?: string;
  minScore?: number;
  search?: string;
  page?: number;
  size?: number;
}

export const leadService = {
  async getLeads(params: LeadFilterParams = {}): Promise<any> {
    return axiosInstance.get('/api/leads', { params });
  },

  async getLead(id: string): Promise<any> {
    return axiosInstance.get(`/api/leads/${id}`);
  },

  async createLead(lead: any): Promise<any> {
    return axiosInstance.post('/api/leads', lead);
  },

  async updateLead(id: string, lead: any): Promise<any> {
    return axiosInstance.put(`/api/leads/${id}`, lead);
  },

  async deleteLead(id: string): Promise<any> {
    return axiosInstance.delete(`/api/leads/${id}`);
  },
};

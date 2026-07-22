import axiosInstance from './axios';

export interface OpportunityFilterParams {
  stage?: string;
  page?: number;
  size?: number;
}

export const opportunityService = {
  async getOpportunities(params: OpportunityFilterParams = {}): Promise<any> {
    return axiosInstance.get('/api/opportunities', { params });
  },

  async getOpportunity(id: string): Promise<any> {
    return axiosInstance.get(`/api/opportunities/${id}`);
  },

  async createOpportunity(opp: any): Promise<any> {
    return axiosInstance.post('/api/opportunities', opp);
  },

  async updateOpportunity(id: string, opp: any): Promise<any> {
    return axiosInstance.put(`/api/opportunities/${id}`, opp);
  },

  async patchOpportunityStage(id: string, stage: string): Promise<any> {
    return axiosInstance.patch(`/api/opportunities/${id}/stage`, { stage });
  },

  async deleteOpportunity(id: string): Promise<any> {
    return axiosInstance.delete(`/api/opportunities/${id}`);
  },
};

import axiosInstance from './axios';

export const organizationService = {
  async getOrganizations(page = 0, size = 10): Promise<any> {
    return axiosInstance.get('/api/organizations', {
      params: { page, size },
    });
  },

  async getOrganization(id: string): Promise<any> {
    return axiosInstance.get(`/api/organizations/${id}`);
  },

  async createOrganization(name: string, subdomain: string): Promise<any> {
    return axiosInstance.post('/api/organizations', { name, subdomain });
  },

  async updateOrganization(id: string, org: any): Promise<any> {
    return axiosInstance.put(`/api/organizations/${id}`, org);
  },

  async lookupSubdomain(subdomain: string): Promise<any> {
    return axiosInstance.get(`/api/organizations/lookup`, {
      params: { subdomain },
    });
  },
};

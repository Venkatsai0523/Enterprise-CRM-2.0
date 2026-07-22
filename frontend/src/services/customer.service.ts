import axiosInstance from './axios';

export const customerService = {
  async getCustomers(page = 0, size = 100): Promise<any> {
    return axiosInstance.get('/api/customers', {
      params: { page, size },
    });
  },

  async getCustomer360(id: string): Promise<any> {
    return axiosInstance.get(`/api/customers/${id}`);
  },
};

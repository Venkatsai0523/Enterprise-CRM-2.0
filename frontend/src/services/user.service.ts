import axiosInstance from './axios';

export const userService = {
  async getMe(): Promise<any> {
    return axiosInstance.get('/api/users/me');
  },
};

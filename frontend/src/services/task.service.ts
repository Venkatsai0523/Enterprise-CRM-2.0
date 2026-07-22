import axiosInstance from './axios';

export interface TaskFilterParams {
  assignedTo?: string;
  status?: string;
  relatedToType?: string;
  relatedToId?: string;
  page?: number;
  size?: number;
}

export const taskService = {
  async getTasks(params: TaskFilterParams = {}): Promise<any> {
    return axiosInstance.get('/api/tasks', { params });
  },

  async getTask(id: string): Promise<any> {
    return axiosInstance.get(`/api/tasks/${id}`);
  },

  async createTask(task: any): Promise<any> {
    return axiosInstance.post('/api/tasks', task);
  },

  async updateTask(id: string, task: any): Promise<any> {
    return axiosInstance.put(`/api/tasks/${id}`, task);
  },

  async patchTaskStatus(id: string, status: string): Promise<any> {
    return axiosInstance.patch(`/api/tasks/${id}/status`, null, {
      params: { status },
    });
  },

  async deleteTask(id: string): Promise<any> {
    return axiosInstance.delete(`/api/tasks/${id}`);
  },
};

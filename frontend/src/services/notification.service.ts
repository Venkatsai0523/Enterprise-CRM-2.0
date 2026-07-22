import axiosInstance from './axios';

export interface NotificationFilterParams {
  recipientId: string;
  page?: number;
  size?: number;
}

export const notificationService = {
  async getNotifications(params: NotificationFilterParams): Promise<any> {
    return axiosInstance.get('/api/notifications', { params });
  },

  async patchNotificationRead(id: string): Promise<any> {
    return axiosInstance.patch(`/api/notifications/${id}/read`);
  },
};

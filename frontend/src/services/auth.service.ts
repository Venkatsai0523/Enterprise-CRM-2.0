import axiosInstance from './axios';

export const authService = {
  async login(email: string, passwordHash: string): Promise<any> {
    const data = await axiosInstance.post('/api/auth/login', {
      email,
      password: passwordHash,
    });
    if (data && (data as any).token) {
      localStorage.setItem('token', (data as any).token);
      localStorage.setItem('refreshToken', (data as any).refreshToken);
      localStorage.setItem('role', (data as any).roles?.[0] || 'ROLE_SALES_REP');
      localStorage.setItem('email', (data as any).email);
    }
    return data;
  },

  async register(
    email: string,
    passwordHash: string,
    firstName: string,
    lastName: string,
    roleName?: string,
    organizationId?: string
  ): Promise<any> {
    return axiosInstance.post('/api/auth/register', {
      email,
      password: passwordHash,
      firstName,
      lastName,
      roleName: roleName || 'ROLE_SALES_REP',
      organizationId,
    });
  },

  async refresh(refreshToken: string): Promise<any> {
    return axiosInstance.post('/api/auth/refresh', {
      refreshToken,
    });
  },

  logout() {
    localStorage.clear();
  },
};

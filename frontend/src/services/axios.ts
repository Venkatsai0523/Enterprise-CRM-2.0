import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach Bearer Token
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response Interceptor: Unwrap Standard Response Envelope & Handle Token Refresh
axiosInstance.interceptors.response.use(
  (response) => {
    // Check if the response follows the StandardResponse envelope
    if (response.data && response.data.hasOwnProperty('success')) {
      if (response.data.success) {
        return response.data.data;
      } else {
        return Promise.reject(new Error(response.data.message || 'Operation failed'));
      }
    }
    return response.data;
  },
  async (error) => {
    const originalRequest = error.config;

    // Detect 401 Unauthorized errors and prevent infinite refresh loops
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axiosInstance(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        // Clear session and redirect to login
        localStorage.clear();
        window.location.href = '/login'; // Or navigate back to Login state
        return Promise.reject(error);
      }

      try {
        // Refresh token endpoint expects {"refreshToken": "..."} inside TokenRefreshRequestDto
        const refreshResponse = await axios.post('http://localhost:8080/api/auth/refresh', {
          refreshToken,
        });

        // The refresh token endpoint response also follows the StandardResponse envelope
        const payload = refreshResponse.data.success ? refreshResponse.data.data : refreshResponse.data;
        const newToken = payload.token;
        const newRefreshToken = payload.refreshToken;

        if (newToken) {
          localStorage.setItem('token', newToken);
          if (newRefreshToken) {
            localStorage.setItem('refreshToken', newRefreshToken);
          }
          axiosInstance.defaults.headers.common.Authorization = `Bearer ${newToken}`;
          processQueue(null, newToken);
          isRefreshing = false;
          return axiosInstance(originalRequest);
        }
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // Unwrap standard error response message if available
    const errorMsg = error.response?.data?.message || error.message || 'An unexpected error occurred';
    return Promise.reject(new Error(errorMsg));
  }
);

export default axiosInstance;

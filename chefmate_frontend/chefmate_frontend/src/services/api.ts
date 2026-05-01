import axios from 'axios';
import { API_URL } from '../utils/apiConfig';
import { TokenManager } from './tokenManager';

export const api = axios.create({
  baseURL: API_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
let failedQueue: Array<{
  resolve: () => void;
  reject: (reason?: any) => void;
}> = [];

const processQueue = (error: any) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve();
  });
  failedQueue = [];
};

const retryRequest = (config: any) => {
  return api({
    method: config.method,
    url: config.url,
    data: config.data,
    params: config.params,
    headers: { ...config.headers },
    withCredentials: true,
  });
};

const redirectToLogin = () => {
  isRefreshing = false;
  TokenManager.clear();
  window.location.href = '/';
};

const doRefresh = async (): Promise<void> => {
  const refreshResponse = await api.post('/users/refresh');
  if (refreshResponse.data?.success === false) {
    throw new Error('Session expired');
  }
  // Update expiry from response if available, otherwise keep current
  const expiresIn: number | undefined = refreshResponse.data?.expires_in_minutes;
  if (expiresIn) {
    TokenManager.setExpiry(expiresIn);
  }
  await Promise.resolve();
};

// ── Proactive interceptor (REQUEST) ──────────────────────────────────────────
// Refreshes the token BEFORE a request if it's expiring in < 60s
api.interceptors.request.use(async (config) => {
  const isAuthRoute =
    config.url?.includes('/users/login') ||
    config.url?.includes('/users/refresh') ||
    config.url?.includes('/users/register');

  if (isAuthRoute || !TokenManager.hasSession()) return config;

  if (TokenManager.isExpiringSoon()) {
    if (isRefreshing) {
      await new Promise<void>((resolve, reject) => failedQueue.push({ resolve, reject }));
      return config;
    }
    isRefreshing = true;
    try {
      await doRefresh();
      processQueue(null);
    } catch {
      processQueue(new Error('Session expired'));
      redirectToLogin();
      throw new axios.Cancel('Session expired');
    } finally {
      isRefreshing = false;
    }
  }

  return config;
});

// ── Reactive interceptor (RESPONSE) — second line of defense ─────────────────
// Catches unexpected 401s (clock drift, server-side invalidation, etc.)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      error.response?.status !== 401 ||
      originalRequest._retry ||
      originalRequest.url?.includes('/users/login') ||
      originalRequest.url?.includes('/users/refresh')
    ) {
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise<void>((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then(() => retryRequest(originalRequest));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      await doRefresh();
      processQueue(null);
      return retryRequest(originalRequest);
    } catch (refreshError: any) {
      processQueue(refreshError);
      redirectToLogin();
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

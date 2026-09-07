import axios from 'axios';

/**
 * api.js - Axios instance configured for MentR backend.
 *
 * WHY THIS EXISTS:
 * Instead of writing the full backend URL for every API call,
 * we configure a single Axios instance with the base URL.
 * We also add an interceptor to automatically attach the JWT token
 * to every request — so we don't have to do it manually each time.
 *
 * HOW IT WORKS:
 * Every request made using this 'api' instance will:
 * 1. Have baseURL = 'http://localhost:8080'
 * 2. Automatically include the Authorization header with the JWT token
 *
 * USAGE EXAMPLE:
 * import api from '../services/api';
 * const response = await api.get('/api/student/1/projects');
 */
const api = axios.create({
  // REACT_APP_API_URL is set at build time on the deployment platform (e.g. Vercel)
  // to point at the deployed backend. Locally, it's unset and falls back to 8081,
  // so `npm start` continues to work exactly as before.
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8081',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request Interceptor - runs before EVERY request.
 * Reads the JWT token from localStorage and adds it to the header.
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor - runs after EVERY response.
 * If the server returns 401 (unauthorized) on a PROTECTED endpoint, clear
 * localStorage and redirect to login (expired JWT).
 *
 * We deliberately skip the redirect for /api/auth/ endpoints so that a wrong
 * password on the login page shows the error message instead of silently
 * refreshing.
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthEndpoint = error.config && error.config.url &&
      error.config.url.includes('/api/auth/');
    if (error.response && error.response.status === 401 && !isAuthEndpoint) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

import axios from 'axios';
import useAuthStore from '../store/useAuthStore';
import { toast } from '../store/useToastStore';

/**
 * 统一 axios 实例：
 * - 请求拦截器：自动附加 Authorization: Bearer <token>
 * - 响应拦截器：解包统一响应体 {code,message,data}，code!==0 时 reject 抛出 message
 * - 401 / code=40100 未登录：清空登录态并跳转 /login
 */
const client = axios.create({
  baseURL: '',
  timeout: 15000,
});

/** 清登录态并跳转登录页（携带回跳地址） */
function redirectToLogin() {
  useAuthStore.getState().logout();
  if (!window.location.pathname.startsWith('/login')) {
    const redirect = encodeURIComponent(window.location.pathname + window.location.search);
    window.location.href = `/login?redirect=${redirect}`;
  }
}

// 请求拦截器：附加 token
client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：解包统一响应体
client.interceptors.response.use(
  (response) => {
    const body = response.data;
    // 非标准结构直接透传
    if (body === null || typeof body !== 'object' || !('code' in body)) return body;
    if (body.code === 0) return body.data;
    if (body.code === 40100) {
      toast.error('请先登录');
      redirectToLogin();
    }
    const err = new Error(body.message || '请求失败');
    err.code = body.code;
    return Promise.reject(err);
  },
  (error) => {
    const status = error.response?.status;
    const body = error.response?.data;
    if (status === 401 || body?.code === 40100) {
      toast.error('登录已过期，请重新登录');
      redirectToLogin();
      const err = new Error(body?.message || '未登录');
      err.code = 40100;
      return Promise.reject(err);
    }
    const err = new Error(body?.message || (status ? `服务异常(${status})` : '网络连接失败'));
    err.code = body?.code ?? status;
    return Promise.reject(err);
  }
);

export default client;

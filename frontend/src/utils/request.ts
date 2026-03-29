import axios from 'axios';
import { useUserStore } from '@/stores/user';
import router from '@/router';
import { ElMessage } from 'element-plus';

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
});

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

service.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败');
      return Promise.reject(new Error(res.msg || '请求失败'));
    }
    return res;
  },
  (error) => {
    if (error.response?.status === 401) {
      if (error.config?.url?.includes('/auth/login')) {
        ElMessage.error(error.response?.data?.msg || '用户名或密码错误');
      } else {
        const userStore = useUserStore();
        userStore.logout();
        router.push('/login');
      }
    } else if (error.response?.status === 403) {
      ElMessage.error('无权限访问');
      router.push('/403');
    } else {
      ElMessage.error(error.message || '网络错误');
    }
    return Promise.reject(error);
  },
);

export default service;

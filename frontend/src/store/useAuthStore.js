import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * 登录态：token + 用户信息，persist 持久化到 localStorage
 */
const useAuthStore = create(
  persist(
    (set) => ({
      token: '',
      user: null,
      /** 登录成功：写入 token 与用户信息 */
      setAuth: (token, user) => set({ token, user }),
      /** 更新用户信息（改昵称等场景） */
      setUser: (user) => set({ user }),
      /** 退出登录：清空 */
      logout: () => set({ token: '', user: null }),
    }),
    { name: 'food-app-auth' }
  )
);

export default useAuthStore;

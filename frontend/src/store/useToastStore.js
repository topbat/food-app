import { create } from 'zustand';

let seq = 0;

/**
 * 全局 Toast 状态：成功/错误轻提示队列
 */
const useToastStore = create((set) => ({
  toasts: [],
  push: (message, type = 'success') => {
    const id = ++seq;
    set((s) => ({ toasts: [...s.toasts, { id, message, type }] }));
    // 2.4 秒后自动消失
    setTimeout(() => {
      set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) }));
    }, 2400);
  },
  remove: (id) => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),
}));

/** 任意位置调用的快捷方法（非组件内也可用，如 axios 拦截器） */
export const toast = {
  success: (msg) => useToastStore.getState().push(msg, 'success'),
  error: (msg) => useToastStore.getState().push(msg, 'error'),
};

export default useToastStore;

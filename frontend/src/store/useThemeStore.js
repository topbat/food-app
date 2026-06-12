import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * 主题状态：'light' | 'dark' | 'system'
 * - 默认 system：跟随操作系统 prefers-color-scheme
 * - persist 到 localStorage（key 与 index.html 首屏脚本约定一致：food-app-theme）
 * - 切换时同步 document.documentElement 的 .dark 类与 <meta name="theme-color">
 * - system 模式下监听系统主题变化实时切换
 */

/** 各主题对应的浏览器状态栏色（与 index.css 中 --c-paper 一致） */
const THEME_COLORS = { light: '#F7F3EB', dark: '#171513' };

/** 当前系统是否为暗色偏好 */
function systemPrefersDark() {
  return typeof window !== 'undefined' && !!window.matchMedia?.('(prefers-color-scheme: dark)').matches;
}

/** 把 mode 解析为实际生效主题 */
function resolveTheme(mode) {
  if (mode === 'dark') return 'dark';
  if (mode === 'light') return 'light';
  return systemPrefersDark() ? 'dark' : 'light';
}

/** 应用主题到 DOM：增删 .dark 类 + 同步 meta theme-color，返回实际生效主题 */
function applyTheme(mode) {
  const resolved = resolveTheme(mode);
  document.documentElement.classList.toggle('dark', resolved === 'dark');
  const meta = document.querySelector('meta[name="theme-color"]');
  if (meta) meta.setAttribute('content', THEME_COLORS[resolved]);
  return resolved;
}

const useThemeStore = create(
  persist(
    (set) => ({
      mode: 'system', // 用户选择的模式
      resolved: 'light', // 实际生效主题（派生值，不持久化）
      /** 切换主题模式并立即应用 */
      setMode: (mode) => set({ mode, resolved: applyTheme(mode) }),
    }),
    {
      name: 'food-app-theme',
      partialize: (s) => ({ mode: s.mode }), // 只持久化 mode
    }
  )
);

// ===== 模块加载即初始化（persist 同步恢复后应用一次） =====
if (typeof window !== 'undefined') {
  useThemeStore.setState({ resolved: applyTheme(useThemeStore.getState().mode) });
  // system 模式下监听系统主题切换
  const mq = window.matchMedia?.('(prefers-color-scheme: dark)');
  const onSystemChange = () => {
    const { mode } = useThemeStore.getState();
    if (mode === 'system') useThemeStore.setState({ resolved: applyTheme('system') });
  };
  // 兼容旧浏览器的 addListener
  if (mq?.addEventListener) mq.addEventListener('change', onSystemChange);
  else mq?.addListener?.(onSystemChange);
}

export default useThemeStore;

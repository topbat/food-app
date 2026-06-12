import useThemeStore from '../store/useThemeStore';

/** 三种模式循环顺序：亮色 → 暗色 → 跟随系统 */
const MODES = [
  { key: 'light', icon: '☀️', label: '亮色' },
  { key: 'dark', icon: '🌙', label: '暗色' },
  { key: 'system', icon: '🌗', label: '跟随系统' },
];

/**
 * 主题切换按钮（太阳/月亮/自动）：点击在 亮 → 暗 → 跟随系统 间循环
 */
export default function ThemeToggle({ className = '' }) {
  const mode = useThemeStore((s) => s.mode);
  const setMode = useThemeStore((s) => s.setMode);

  const idx = Math.max(0, MODES.findIndex((m) => m.key === mode));
  const cur = MODES[idx];
  const next = MODES[(idx + 1) % MODES.length];

  return (
    <button
      type="button"
      onClick={() => setMode(next.key)}
      title={`当前：${cur.label}，点击切换为${next.label}`}
      aria-label={`切换主题（当前${cur.label}）`}
      className={`w-9 h-9 rounded-full bg-ink/5 hover:bg-ink/10 flex items-center justify-center text-base transition active:scale-95 ${className}`}
    >
      {cur.icon}
    </button>
  );
}

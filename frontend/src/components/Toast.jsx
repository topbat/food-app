import useToastStore from '../store/useToastStore';

/**
 * 全局 Toast 容器：顶部居中浮层，成功黛绿 / 错误朱砂
 */
export default function ToastContainer() {
  const toasts = useToastStore((s) => s.toasts);
  if (!toasts.length) return null;
  return (
    <div className="fixed top-4 left-0 right-0 z-[999] flex flex-col items-center gap-2 px-4 pointer-events-none pt-safe">
      {toasts.map((t) => (
        <div
          key={t.id}
          className={`animate-fade-up max-w-sm px-4 py-2.5 rounded-xl shadow-lift text-sm text-white flex items-center gap-2 ${
            t.type === 'error' ? 'bg-cinnabar' : 'bg-jade'
          }`}
        >
          <span>{t.type === 'error' ? '✕' : '✓'}</span>
          <span>{t.message}</span>
        </div>
      ))}
    </div>
  );
}

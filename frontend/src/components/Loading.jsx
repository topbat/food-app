/**
 * 加载骨架屏：通用占位（页面懒加载 / 列表加载中）
 */
export function PageLoading() {
  return (
    <div className="max-w-5xl mx-auto px-4 py-6 space-y-4 animate-pulse">
      <div className="h-8 w-40 rounded-xl bg-ink/5" />
      <div className="h-44 rounded-2xl bg-ink/5" />
      <div className="grid grid-cols-2 gap-4">
        <div className="h-52 rounded-2xl bg-ink/5" />
        <div className="h-52 rounded-2xl bg-ink/5" />
      </div>
    </div>
  );
}

/** 菜谱卡片骨架（feed 加载中） */
export function CardSkeleton({ count = 4 }) {
  return (
    <div className="grid grid-cols-2 md:grid-cols-3 gap-3 md:gap-4 animate-pulse">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="card overflow-hidden">
          <div className="h-32 bg-ink/5" />
          <div className="p-3 space-y-2">
            <div className="h-4 w-3/4 rounded bg-ink/5" />
            <div className="h-3 w-1/2 rounded bg-ink/5" />
          </div>
        </div>
      ))}
    </div>
  );
}

/** 行内小型加载指示 */
export function Spinner({ text = '加载中…' }) {
  return (
    <div className="flex items-center justify-center gap-2 py-6 text-mute text-sm">
      <span className="inline-block w-4 h-4 border-2 border-cinnabar/30 border-t-cinnabar rounded-full animate-spin" />
      {text}
    </div>
  );
}

/**
 * SVG 热量环形图：手写 circle stroke-dasharray
 * value: 当前值；max: 满环对应值；中心展示 kcal 数字
 * over=true 时（超标）整环朱砂红
 */
export default function CalorieRing({
  value = 0,
  max = 1000,
  size = 120,
  strokeWidth = 10,
  label = 'kcal',
  sub = '',
  over = false,
}) {
  const r = (size - strokeWidth) / 2;
  const c = 2 * Math.PI * r;
  const ratio = Math.max(0, Math.min(1, max > 0 ? value / max : 0));
  const dash = c * ratio;

  return (
    <div className="relative inline-flex items-center justify-center" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        {/* 底环：墨色低透明度（颜色随主题 token 变化） */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          className="stroke-ink/10"
          strokeWidth={strokeWidth}
        />
        {/* 进度环：常规暖橙，超标朱砂红 */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          className={over ? 'stroke-cinnabar' : 'stroke-warmth'}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={`${dash} ${c - dash}`}
          style={{ transition: 'stroke-dasharray 0.8s ease, stroke 0.4s ease' }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className={`font-serif font-bold leading-none ${over ? 'text-cinnabar' : 'text-ink'}`} style={{ fontSize: size / 4.2 }}>
          {Math.round(value)}
        </span>
        <span className="text-mute" style={{ fontSize: Math.max(10, size / 11) }}>
          {label}
        </span>
        {sub ? (
          <span className="text-mute" style={{ fontSize: Math.max(9, size / 13) }}>
            {sub}
          </span>
        ) : null}
      </div>
    </div>
  );
}

/**
 * 中式色票标签 chip：淡色底 + 深色字
 * tone: jade(黛绿) / cinnabar(朱砂) / ochre(赭石) / warmth(暖橙) / mute(灰)
 * active + onClick 时呈可选中样式
 */
const TONES = {
  jade: 'bg-jade/10 text-jade',
  cinnabar: 'bg-cinnabar/10 text-cinnabar',
  ochre: 'bg-ochre/10 text-ochre',
  warmth: 'bg-warmth/10 text-warmth',
  mute: 'bg-ink/5 text-mute',
};

export default function TagChip({ children, tone = 'jade', active = false, onClick, className = '' }) {
  const base = `inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs whitespace-nowrap transition ${className}`;
  if (onClick) {
    return (
      <button
        type="button"
        onClick={onClick}
        className={`${base} active:scale-95 ${
          active ? 'bg-cinnabar text-white shadow-seal' : TONES[tone] || TONES.jade
        }`}
      >
        {children}
      </button>
    );
  }
  return <span className={`${base} ${TONES[tone] || TONES.jade}`}>{children}</span>;
}

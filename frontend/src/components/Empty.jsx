/**
 * 空状态：emoji + 文案，可附操作按钮
 */
export default function Empty({ emoji = '🍃', text = '这里空空如也', action = null }) {
  return (
    <div className="flex flex-col items-center justify-center py-14 gap-3 text-mute">
      <span className="text-5xl">{emoji}</span>
      <p className="text-sm">{text}</p>
      {action}
    </div>
  );
}

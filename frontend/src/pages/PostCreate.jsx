import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { createPost, checkin } from '../api/social';
import useAuthStore from '../store/useAuthStore';
import { toast } from '../store/useToastStore';
import { POST_TYPES, PLACEHOLDER_IMG } from '../utils/constants';

/**
 * 发帖打卡页（需登录）：
 * - 内容文本域 + 多图 URL 列表（带预览缩略图）+ postType 选择 + 可选关联菜谱
 * - 提交发帖成功后自动调打卡接口，弹出「连续打卡 X 天」+ 新徽章动效
 * - 厨房模式完成后跳转过来时，query 里的 recipeId 自动预填
 */
export default function PostCreate() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const token = useAuthStore((s) => s.token);

  const recipeIdFromQuery = params.get('recipeId') || '';
  const [content, setContent] = useState('');
  const [images, setImages] = useState(['']); // 图片 URL 输入列表（至少留一个输入框）
  const [postType, setPostType] = useState(POST_TYPES[0].value);
  const [recipeId, setRecipeId] = useState(recipeIdFromQuery);
  const [submitting, setSubmitting] = useState(false);
  // 打卡结果浮层：{continuousDays, newBadges:[{badgeName,icon}]}
  const [checkinResult, setCheckinResult] = useState(null);

  // 鉴权页面：未登录跳转登录页（带回跳地址）
  useEffect(() => {
    if (!token) {
      toast.error('请先登录');
      const back = `/community/post${recipeIdFromQuery ? `?recipeId=${recipeIdFromQuery}` : ''}`;
      navigate(`/login?redirect=${encodeURIComponent(back)}`, { replace: true });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  /** 修改第 i 个图片 URL */
  const setImageAt = (i, val) =>
    setImages((arr) => arr.map((u, idx) => (idx === i ? val : u)));

  /** 删除第 i 个图片输入（至少保留一个） */
  const removeImageAt = (i) =>
    setImages((arr) => (arr.length <= 1 ? [''] : arr.filter((_, idx) => idx !== i)));

  /** 提交：先发帖，成功后再打卡（关键交互：打卡返回连续天数与新徽章） */
  const handleSubmit = async () => {
    if (!content.trim()) return toast.error('写点内容再发布吧');
    const imageUrls = images.map((u) => u.trim()).filter(Boolean);
    setSubmitting(true);
    try {
      // 1) 发帖
      await createPost({
        recipeId: recipeId ? Number(recipeId) : undefined,
        content: content.trim(),
        imageUrls,
        postType,
      });
      // 2) 打卡：计算连续天数并可能触发新徽章
      try {
        const res = await checkin({
          recipeId: recipeId ? Number(recipeId) : undefined,
          note: content.trim().slice(0, 50),
        });
        setCheckinResult(res || { continuousDays: 1, newBadges: [] });
      } catch {
        // 打卡失败（如今日已打卡）不阻塞发帖流程，直接回社区
        toast.success('发布成功');
        navigate('/community', { replace: true });
      }
    } catch (err) {
      toast.error(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto px-4 pt-5 md:pt-8 space-y-5 pb-10">
      {/* 标题栏 */}
      <div className="flex items-center gap-3 animate-fade-up">
        <button
          onClick={() => navigate(-1)}
          aria-label="返回"
          className="w-9 h-9 rounded-full bg-ink/5 text-ink/60 flex items-center justify-center active:scale-95"
        >
          ←
        </button>
        <h1 className="font-serif text-xl md:text-2xl font-bold">发布打卡</h1>
      </div>

      {/* 帖子类型选择 */}
      <div className="flex gap-2">
        {POST_TYPES.map((t) => (
          <button
            key={t.value}
            onClick={() => setPostType(t.value)}
            className={`flex-1 py-2.5 rounded-2xl text-xs md:text-sm transition active:scale-95 ${
              postType === t.value ? 'bg-cinnabar text-white shadow-seal' : 'card text-ink/70'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* 内容文本域 */}
      <div className="card p-4">
        <textarea
          className="w-full bg-transparent text-sm leading-relaxed placeholder:text-mute/70 resize-none"
          rows={5}
          maxLength={500}
          placeholder="今天做了什么好菜？记录一下出锅瞬间…"
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <p className="text-right text-[10px] text-mute">{content.length}/500</p>
      </div>

      {/* 图片 URL 列表（每个输入框带预览缩略图） */}
      <section className="card p-4 space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="font-serif text-sm font-semibold">配图（图片链接）</h2>
          <button
            className="seal-badge px-2.5 py-1 text-xs active:scale-95"
            onClick={() => setImages((arr) => [...arr, ''])}
          >
            ＋ 加一张
          </button>
        </div>
        {images.map((url, i) => (
          <div key={i} className="flex items-center gap-2">
            {/* 预览缩略图 */}
            <img
              src={url.trim() || PLACEHOLDER_IMG}
              onError={(e) => (e.currentTarget.src = PLACEHOLDER_IMG)}
              alt=""
              className="w-12 h-12 rounded-xl object-cover bg-ink/5 shrink-0"
            />
            <input
              className="input-base flex-1"
              placeholder="https://… 图片地址"
              value={url}
              onChange={(e) => setImageAt(i, e.target.value)}
            />
            <button
              onClick={() => removeImageAt(i)}
              aria-label="删除图片"
              className="w-8 h-8 rounded-full bg-ink/5 text-mute text-xs flex items-center justify-center active:scale-95 shrink-0"
            >
              ✕
            </button>
          </div>
        ))}
      </section>

      {/* 关联菜谱（可选，从厨房模式跳转时预填） */}
      <section className="card p-4 space-y-2">
        <h2 className="font-serif text-sm font-semibold">关联菜谱（可选）</h2>
        <input
          className="input-base"
          type="number"
          placeholder="填写菜谱 ID，让大家能跳转看做法"
          value={recipeId}
          onChange={(e) => setRecipeId(e.target.value)}
        />
        {recipeIdFromQuery && (
          <p className="text-[11px] text-jade">✓ 已自动关联刚做完的菜谱</p>
        )}
      </section>

      {/* 提交 */}
      <button
        className="btn-primary w-full py-4 text-lg font-serif tracking-widest shadow-lift"
        disabled={submitting}
        onClick={handleSubmit}
      >
        {submitting ? '发布中…' : '发布并打卡'}
      </button>

      {/* ===== 打卡成功浮层：连续天数 + 新徽章动效 ===== */}
      {checkinResult && (
        <div className="fixed inset-0 z-50 bg-ink/60 backdrop-blur-sm flex items-center justify-center px-6">
          <div className="bg-card w-full max-w-sm rounded-3xl p-7 text-center space-y-4 animate-pop">
            <span className="seal-badge w-16 h-16 text-3xl mx-auto animate-pop">🔥</span>
            <h2 className="font-serif text-2xl font-bold">打卡成功！</h2>
            <p className="text-sm text-ink/70">
              已连续打卡 <b className="font-serif text-3xl text-cinnabar">{checkinResult.continuousDays}</b> 天
            </p>
            {/* 新获得徽章：逐个 pop 动效 */}
            {checkinResult.newBadges?.length > 0 && (
              <div className="bg-paper rounded-2xl p-4 space-y-2">
                <p className="text-xs text-warmth">🎉 解锁新徽章</p>
                <div className="flex justify-center gap-4 flex-wrap">
                  {checkinResult.newBadges.map((b, i) => (
                    <div
                      key={i}
                      className="flex flex-col items-center gap-1 animate-pop"
                      style={{ animationDelay: `${i * 0.18}s` }}
                    >
                      <span className="text-3xl">{b.icon || '🏅'}</span>
                      <span className="text-[11px] text-ink/70">{b.badgeName}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
            <button
              className="btn-primary w-full py-3 font-serif"
              onClick={() => navigate('/community', { replace: true })}
            >
              去社区看看
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

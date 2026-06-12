import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getRecipeDetail, viewRecipe, substituteRecipe } from '../api/recipe';
import { getRating, rateRecipe, getCommentList, addComment } from '../api/social';
import { createSession } from '../api/kitchen';
import CalorieRing from '../components/CalorieRing';
import TagChip from '../components/TagChip';
import SmartImage from '../components/SmartImage';
import Empty from '../components/Empty';
import { PageLoading, Spinner } from '../components/Loading';
import useAuthStore from '../store/useAuthStore';
import { toast } from '../store/useToastStore';
import { DIFFICULTY, PHASES, SUBSTITUTE_SCENES } from '../utils/constants';

/** 数字滚动动效：从 0 滚到目标值 */
function NumberTicker({ value, duration = 900, className = '' }) {
  const [display, setDisplay] = useState(0);
  const raf = useRef(0);
  useEffect(() => {
    const start = performance.now();
    const tick = (now) => {
      const p = Math.min(1, (now - start) / duration);
      // easeOutCubic
      setDisplay(Math.round(value * (1 - Math.pow(1 - p, 3))));
      if (p < 1) raf.current = requestAnimationFrame(tick);
    };
    raf.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf.current);
  }, [value, duration]);
  return <span className={className}>{display}</span>;
}

/** 三大营养素横向占比条 */
function MacroBar({ carbs = 0, protein = 0, fat = 0 }) {
  const total = carbs + protein + fat || 1;
  const items = [
    { label: '碳水', value: carbs, color: 'bg-warmth', text: 'text-warmth' },
    { label: '蛋白质', value: protein, color: 'bg-jade', text: 'text-jade' },
    { label: '脂肪', value: fat, color: 'bg-ochre', text: 'text-ochre' },
  ];
  return (
    <div className="flex-1 space-y-2">
      <div className="flex h-3 rounded-full overflow-hidden bg-ink/5">
        {items.map((it) => (
          <div
            key={it.label}
            className={`${it.color} transition-all duration-700`}
            style={{ width: `${(it.value / total) * 100}%` }}
          />
        ))}
      </div>
      <div className="flex justify-between">
        {items.map((it) => (
          <span key={it.label} className="text-[11px] text-mute">
            <i className={`inline-block w-2 h-2 rounded-full ${it.color} mr-1`} />
            {it.label} <b className={it.text}>{it.value}g</b>
          </span>
        ))}
      </div>
    </div>
  );
}

/** 五角星评分（展示 + 可点击打分） */
function Stars({ score = 0, onRate, size = 'text-base' }) {
  return (
    <span className={`inline-flex ${size}`}>
      {[1, 2, 3, 4, 5].map((i) => (
        <button
          key={i}
          type="button"
          disabled={!onRate}
          onClick={() => onRate && onRate(i)}
          className={`${onRate ? 'active:scale-110 transition cursor-pointer' : 'cursor-default'} ${
            i <= Math.round(score) ? 'text-gold' : 'text-ink/15' // 金棕琥珀星星点缀
          }`}
        >
          ★
        </button>
      ))}
    </span>
  );
}

/**
 * 菜谱详情页：热量环 / 营养占比 / 标签 / 食材 / 智能替换 / 五步法预览 / 评分评论 / 开始烹饪
 */
export default function RecipeDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const token = useAuthStore((s) => s.token);

  const [detail, setDetail] = useState(null);
  const [rating, setRating] = useState({ avgScore: 0, ratingCount: 0 });
  const [comments, setComments] = useState({ list: [], total: 0 });
  const [commentPage, setCommentPage] = useState(1);
  const [commentText, setCommentText] = useState('');
  const [openPhases, setOpenPhases] = useState({ PREPARE: true });

  // 智能替换弹层
  const [subOpen, setSubOpen] = useState(false);
  const [subScene, setSubScene] = useState('');
  const [subResult, setSubResult] = useState(null);
  const [subLoading, setSubLoading] = useState(false);

  // 开始烹饪
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [starting, setStarting] = useState(false);

  /** 加载评论 */
  const loadComments = async (p = 1) => {
    try {
      const data = await getCommentList({ targetType: 1, targetId: id, page: p, size: 10 });
      setComments((prev) => ({
        total: data?.total || 0,
        list: p === 1 ? data?.list || [] : [...prev.list, ...(data?.list || [])],
      }));
      setCommentPage(p);
    } catch {
      /* 评论失败静默 */
    }
  };

  useEffect(() => {
    // 进入即记一次浏览（失败静默）
    viewRecipe(id).catch(() => {});
    getRecipeDetail(id)
      .then(setDetail)
      .catch((err) => toast.error(err.message));
    getRating(id).then((d) => setRating(d || { avgScore: 0, ratingCount: 0 })).catch(() => {});
    loadComments(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (!detail) return <PageLoading />;
  const info = detail.info || {};
  const requireLogin = () => {
    toast.error('请先登录');
    navigate(`/login?redirect=${encodeURIComponent(`/recipe/${id}`)}`);
  };

  /** 打分 */
  const handleRate = async (score) => {
    if (!token) return requireLogin();
    try {
      await rateRecipe({ recipeId: Number(id), score });
      toast.success(`已评 ${score} 星`);
      getRating(id).then((d) => setRating(d || rating)).catch(() => {});
    } catch (err) {
      toast.error(err.message);
    }
  };

  /** 发评论 */
  const handleComment = async () => {
    if (!token) return requireLogin();
    if (!commentText.trim()) return toast.error('先写点什么吧');
    try {
      await addComment({ targetType: 1, targetId: Number(id), content: commentText.trim() });
      setCommentText('');
      toast.success('评论成功');
      loadComments(1);
    } catch (err) {
      toast.error(err.message);
    }
  };

  /** 智能替换 */
  const handleSubstitute = async (scene) => {
    setSubScene(scene);
    setSubLoading(true);
    setSubResult(null);
    try {
      const data = await substituteRecipe(id, scene);
      setSubResult(data);
    } catch (err) {
      toast.error(err.message);
    } finally {
      setSubLoading(false);
    }
  };

  /** 开始烹饪：登录校验 → 防误触确认 → 创建会话 */
  const handleStartCooking = async () => {
    setStarting(true);
    try {
      const session = await createSession(Number(id));
      navigate(`/cooking/${session.id}`);
    } catch (err) {
      toast.error(err.message);
    } finally {
      setStarting(false);
      setConfirmOpen(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto pb-28">
      {/* 封面大图 */}
      <div className="relative">
        {/* 详情大图直接看原图 */}
        <SmartImage
          src={info.coverUrl}
          thumb={false}
          alt={info.title}
          className="w-full h-56 md:h-80 object-cover md:rounded-b-2xl"
        />
        <button
          onClick={() => navigate(-1)}
          className="absolute top-3 left-3 w-9 h-9 rounded-full bg-scrim/40 text-white backdrop-blur flex items-center justify-center active:scale-95"
          aria-label="返回"
        >
          ←
        </button>
      </div>

      <div className="px-4 md:px-0 space-y-5 mt-4">
        {/* 标题与基础信息 */}
        <div className="animate-fade-up">
          <div className="flex items-start justify-between gap-3">
            <h1 className="font-serif text-2xl md:text-3xl font-bold leading-snug">{info.title}</h1>
            <span className="seal-badge px-2.5 py-1 text-xs shrink-0 mt-1">{info.cuisineName || '家常'}</span>
          </div>
          <p className="text-xs text-mute mt-2 flex items-center gap-2 flex-wrap">
            <span>难度 {DIFFICULTY[info.difficulty] || '简单'}</span>
            <span className="w-px h-3 bg-ink/15" />
            <span>⏱ {info.totalTimeMin} 分钟</span>
            {info.servings && (
              <>
                <span className="w-px h-3 bg-ink/15" />
                <span>{info.servings} 人份</span>
              </>
            )}
            <span className="w-px h-3 bg-ink/15" />
            <span>👀 {info.viewCount ?? 0}</span>
          </p>
          {info.description && <p className="text-sm text-ink/70 mt-2 leading-relaxed">{info.description}</p>}
        </div>

        {/* 营养卡：热量环 + 宏量占比条 */}
        <section className="card p-4 flex items-center gap-5">
          <CalorieRing value={info.caloriesKcal || 0} max={1500} size={110} sub="单人份" />
          <MacroBar carbs={info.carbsG || 0} protein={info.proteinG || 0} fat={info.fatG || 0} />
        </section>

        {/* 适宜 / 慎用标签 */}
        {(detail.suitableTags?.length || detail.unsuitableTags?.length) ? (
          <section className="card p-4 space-y-3">
            {detail.suitableTags?.length > 0 && (
              <div className="flex flex-wrap items-center gap-1.5">
                <span className="text-xs text-jade font-serif shrink-0">✅ 适宜</span>
                {detail.suitableTags.map((t) => (
                  <TagChip key={t} tone="jade">{t}</TagChip>
                ))}
              </div>
            )}
            {detail.unsuitableTags?.length > 0 && (
              <div className="flex flex-wrap items-center gap-1.5">
                <span className="text-xs text-cinnabar font-serif shrink-0">❌ 慎用</span>
                {detail.unsuitableTags.map((t) => (
                  <TagChip key={t} tone="cinnabar">{t}</TagChip>
                ))}
              </div>
            )}
          </section>
        ) : null}

        {/* 食材清单 + 智能替换入口 */}
        <section className="card p-4">
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-serif text-lg font-semibold">食材清单</h2>
            <button
              className="seal-badge px-3 py-1.5 text-xs active:scale-95 transition"
              onClick={() => {
                setSubOpen(true);
                setSubResult(null);
                setSubScene('');
              }}
            >
              ⇄ 智能替换
            </button>
          </div>
          <ul className="divide-y divide-ink/5">
            {(detail.ingredients || []).map((ing) => (
              <li key={ing.id} className="flex items-center justify-between py-2 text-sm">
                <span className="flex items-center gap-2">
                  {ing.ingredientName}
                  {ing.isEssential ? <TagChip tone="cinnabar" className="!text-[10px]">核心</TagChip> : null}
                </span>
                <span className="text-mute">
                  {ing.amount} {ing.unit}
                </span>
              </li>
            ))}
          </ul>
          {info.tips && (
            <p className="mt-3 text-xs text-ochre bg-ochre/5 rounded-xl p-3 leading-relaxed">💡 {info.tips}</p>
          )}
        </section>

        {/* 五步法预览折叠面板 */}
        <section className="space-y-2">
          <h2 className="font-serif text-lg font-semibold">五步法预览</h2>
          {PHASES.map((ph) => {
            const steps = detail.stepsByPhase?.[ph.key] || [];
            const open = !!openPhases[ph.key];
            return (
              <div key={ph.key} className="card overflow-hidden">
                <button
                  className="w-full flex items-center gap-3 p-3.5"
                  onClick={() => setOpenPhases((o) => ({ ...o, [ph.key]: !o[ph.key] }))}
                >
                  <span className="seal-badge w-8 h-8 text-sm">{ph.name}</span>
                  <span className="font-serif text-sm font-semibold">{ph.fullName}</span>
                  <span className="text-[10px] text-mute">权重 {detail.phaseWeights?.[ph.key] ?? ph.weight}%</span>
                  <span className="ml-auto text-xs text-mute">{steps.length} 步 {open ? '▲' : '▼'}</span>
                </button>
                {open && (
                  <ol className="px-4 pb-4 space-y-3 animate-fade-up">
                    {steps.length === 0 && <p className="text-xs text-mute">本阶段无步骤</p>}
                    {steps.map((s, i) => (
                      <li key={s.id || i} className="flex gap-3">
                        <span className="shrink-0 w-5 h-5 rounded-full bg-ink/5 text-[11px] flex items-center justify-center text-mute">
                          {i + 1}
                        </span>
                        <div className="text-sm">
                          <p className="font-medium">{s.actionTitle}</p>
                          <p className="text-mute text-xs mt-0.5 leading-relaxed">{s.detail}</p>
                          {(s.timerSec > 0 || s.firePower) && (
                            <p className="text-[11px] mt-1 flex gap-2">
                              {s.firePower && <span className="text-cinnabar">🔥 {s.firePower}</span>}
                              {s.timerSec > 0 && <span className="text-warmth">⏱ {Math.round(s.timerSec / 60)} 分钟</span>}
                            </p>
                          )}
                        </div>
                      </li>
                    ))}
                  </ol>
                )}
              </div>
            );
          })}
        </section>

        {/* 评分 */}
        <section className="card p-4 flex items-center justify-between">
          <div>
            <p className="font-serif text-lg font-semibold">
              {rating.avgScore ? Number(rating.avgScore).toFixed(1) : '—'}
              <span className="text-xs text-mute font-sans ml-1">{rating.ratingCount} 人评分</span>
            </p>
            <Stars score={rating.avgScore} />
          </div>
          <div className="text-right">
            <p className="text-xs text-mute mb-1">我来打分</p>
            <Stars score={0} onRate={handleRate} size="text-xl" />
          </div>
        </section>

        {/* 评论区 */}
        <section className="space-y-3">
          <h2 className="font-serif text-lg font-semibold">评论 {comments.total > 0 && `(${comments.total})`}</h2>
          <div className="card p-3 flex gap-2">
            <input
              className="input-base flex-1"
              placeholder={token ? '说说你的做法心得…' : '登录后参与讨论'}
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleComment()}
            />
            <button className="btn-primary px-4 text-sm shrink-0" onClick={handleComment}>
              发送
            </button>
          </div>
          {comments.list.length === 0 ? (
            <Empty emoji="💬" text="还没有评论，来抢沙发" />
          ) : (
            <ul className="space-y-3">
              {comments.list.map((c) => (
                <li key={c.id} className="card p-3.5 flex gap-3">
                  {/* 评论头像：小图走缩略图 */}
                  <SmartImage src={c.avatarUrl} className="w-9 h-9 rounded-full object-cover shrink-0" />
                  <div className="min-w-0">
                    <p className="text-xs text-mute">
                      {c.nickname || '食客'} · {(c.createdAt || '').slice(0, 10)}
                    </p>
                    <p className="text-sm mt-1 leading-relaxed">{c.content}</p>
                  </div>
                </li>
              ))}
            </ul>
          )}
          {comments.list.length < comments.total && (
            <button className="btn-ghost w-full py-2 text-xs" onClick={() => loadComments(commentPage + 1)}>
              查看更多评论
            </button>
          )}
        </section>
      </div>

      {/* ===== 底部固定【开始烹饪】大按钮 ===== */}
      <div className="fixed bottom-14 md:bottom-0 left-0 right-0 z-30 px-4 pb-3 pt-2 pb-safe bg-gradient-to-t from-paper via-paper/95 to-transparent pointer-events-none">
        <div className="max-w-5xl mx-auto pointer-events-auto">
          <button
            className="btn-primary w-full py-4 text-lg font-serif tracking-widest shadow-lift"
            onClick={() => {
              if (!token) return requireLogin();
              setConfirmOpen(true);
            }}
          >
            🍳 开始烹饪
          </button>
        </div>
      </div>

      {/* ===== 防误触确认弹层 ===== */}
      {confirmOpen && (
        <div className="fixed inset-0 z-50 bg-scrim/50 flex items-end md:items-center justify-center" onClick={() => setConfirmOpen(false)}>
          <div
            className="bg-card w-full md:max-w-sm rounded-t-3xl md:rounded-3xl p-6 pb-safe animate-slide-up md:animate-pop"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="font-serif text-xl font-bold text-center">即将进入厨房模式</h3>
            <ul className="text-sm text-mute mt-4 space-y-2">
              <li>· 屏幕将保持常亮，步骤大字显示</li>
              <li>· 左右滑动或语音「下一步」翻页</li>
              <li>· 「煮」环节自动提供倒计时</li>
            </ul>
            <div className="flex gap-3 mt-6">
              <button className="btn-ghost flex-1 py-3" onClick={() => setConfirmOpen(false)}>
                再看看
              </button>
              <button className="btn-primary flex-1 py-3" disabled={starting} onClick={handleStartCooking}>
                {starting ? '准备中…' : '开火！'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ===== 智能替换底部弹层 ===== */}
      {subOpen && (
        <div className="fixed inset-0 z-50 bg-scrim/50 flex items-end md:items-center justify-center" onClick={() => setSubOpen(false)}>
          <div
            className="bg-card w-full md:max-w-md rounded-t-3xl md:rounded-3xl p-6 pb-safe max-h-[80vh] overflow-y-auto animate-slide-up md:animate-pop"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="font-serif text-xl font-bold">智能食材替换</h3>
            <p className="text-xs text-mute mt-1">选择你的饮食场景，自动生成替换方案</p>
            <div className="flex gap-2 mt-4">
              {SUBSTITUTE_SCENES.map((s) => (
                <TagChip key={s} tone="warmth" active={subScene === s} onClick={() => handleSubstitute(s)}>
                  {s}
                </TagChip>
              ))}
            </div>
            {subLoading && <Spinner text="正在计算替换方案…" />}
            {subResult && (
              <div className="mt-5 space-y-3 animate-fade-up">
                {/* 热量差数字动效 */}
                <div className="bg-jade/5 rounded-2xl p-4 text-center">
                  <p className="text-xs text-jade">替换后预计减少</p>
                  <p className="font-serif text-jade font-bold text-3xl mt-1">
                    <NumberTicker value={Math.abs(subResult.calorieDiff || 0)} /> kcal
                  </p>
                  <p className="text-[11px] text-mute mt-1">
                    {subResult.originalCalories} kcal → {subResult.newCalories} kcal
                  </p>
                </div>
                <ul className="space-y-2">
                  {(subResult.substitutions || []).map((s, i) => (
                    <li key={i} className="bg-paper rounded-xl p-3 text-sm">
                      <p className="font-medium">
                        {s.sourceName} <span className="text-warmth mx-1">⇄</span> {s.targetName}
                        {s.calorieDiff ? (
                          <span className="text-[11px] text-jade ml-2">({s.calorieDiff > 0 ? '-' : '+'}{Math.abs(s.calorieDiff)} kcal)</span>
                        ) : null}
                      </p>
                      <p className="text-xs text-mute mt-1">{s.reason}</p>
                    </li>
                  ))}
                  {(subResult.substitutions || []).length === 0 && (
                    <p className="text-xs text-mute text-center py-2">该场景下无需替换，本菜已经很合适啦</p>
                  )}
                </ul>
              </div>
            )}
            <button className="btn-ghost w-full py-2.5 mt-5 text-sm" onClick={() => setSubOpen(false)}>
              关闭
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

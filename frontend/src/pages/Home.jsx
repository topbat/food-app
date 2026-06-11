import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { getHotKeywords, getRecommend } from '../api/search';
import RecipeCard from '../components/RecipeCard';
import TagChip from '../components/TagChip';
import { CardSkeleton } from '../components/Loading';
import Empty from '../components/Empty';
import useAuthStore from '../store/useAuthStore';
import { CUISINES } from '../utils/constants';

/** 按时段问候语 */
function greeting() {
  const h = new Date().getHours();
  if (h < 6) return '夜深了，来点宵夜灵感？';
  if (h < 10) return '早上好，今天做点什么早餐？';
  if (h < 14) return '午安，开火做顿好午饭吧';
  if (h < 18) return '下午好，提前盘算今晚吃什么';
  return '晚上好，烟火气该升起来了';
}

/**
 * 首页：问候语 + 搜索入口 + 热搜 chips + 菜系横滑 + 「为你推荐」feed
 */
export default function Home() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const [hot, setHot] = useState([]);
  const [recommend, setRecommend] = useState({ reason: '', list: [] });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 热搜与推荐并行加载，互不阻塞
    getHotKeywords()
      .then((d) => setHot(d || []))
      .catch(() => {});
    getRecommend(12)
      .then((d) => setRecommend(d || { reason: '', list: [] }))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="max-w-5xl mx-auto px-4 pt-5 md:pt-8 space-y-6">
      {/* 问候语 */}
      <div className="animate-fade-up">
        <p className="text-xs text-mute tracking-widest mb-1">
          食研社 · {user ? `${user.nickname || user.username}` : '客官'}
        </p>
        <h1 className="font-serif text-2xl md:text-3xl font-bold leading-snug">{greeting()}</h1>
      </div>

      {/* 搜索框入口（点击进入搜索页） */}
      <button
        onClick={() => navigate('/search')}
        className="card w-full flex items-center gap-3 px-4 py-3.5 text-mute text-sm active:scale-[0.99] transition md:hover:shadow-lift"
      >
        <span className="text-base">🔍</span>
        搜菜名、食材、人群标签…
        <span className="ml-auto seal-badge px-2 py-0.5 text-[10px]">智能找菜</span>
      </button>

      {/* 热搜 chips */}
      {hot.length > 0 && (
        <div className="flex items-center gap-2 overflow-x-auto no-scrollbar -mx-4 px-4">
          <span className="text-xs text-cinnabar font-serif shrink-0">热搜</span>
          {hot.map((h) => (
            <TagChip
              key={h.keyword}
              tone="cinnabar"
              onClick={() => navigate(`/search?keyword=${encodeURIComponent(h.keyword)}`)}
            >
              🔥 {h.keyword}
            </TagChip>
          ))}
        </div>
      )}

      {/* 菜系横滑分类 */}
      <section>
        <h2 className="font-serif text-lg font-semibold mb-3">寻味八方</h2>
        <div className="flex gap-3 overflow-x-auto no-scrollbar -mx-4 px-4 pb-1 md:grid md:grid-cols-9 md:overflow-visible">
          {CUISINES.map((c) => (
            <button
              key={c.type}
              onClick={() => navigate(`/search?cuisineType=${c.type}&cuisineName=${c.name}`)}
              className="card shrink-0 w-[72px] md:w-auto py-3 flex flex-col items-center gap-1.5 active:scale-95 md:hover:-translate-y-1 md:hover:shadow-lift transition"
            >
              <span className="text-2xl">{c.emoji}</span>
              <span className="font-serif text-xs text-ink/80">{c.name}</span>
            </button>
          ))}
        </div>
      </section>

      {/* 为你推荐 */}
      <section>
        <div className="flex items-baseline justify-between mb-3">
          <h2 className="font-serif text-lg font-semibold">为你推荐</h2>
          {recommend.reason && <span className="text-[11px] text-warmth">{recommend.reason}</span>}
        </div>
        {loading ? (
          <CardSkeleton count={6} />
        ) : recommend.list?.length ? (
          <div className="grid grid-cols-2 md:grid-cols-3 gap-3 md:gap-4">
            {recommend.list.map((r) => (
              <RecipeCard key={r.id} recipe={r} />
            ))}
          </div>
        ) : (
          <Empty
            emoji="🥢"
            text="暂时没有推荐内容"
            action={
              <Link to="/search" className="btn-primary px-5 py-2 text-sm">
                去搜索看看
              </Link>
            }
          />
        )}
      </section>
    </div>
  );
}

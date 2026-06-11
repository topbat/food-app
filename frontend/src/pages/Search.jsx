import { useEffect, useMemo, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { searchRecipes, getSearchHistory, clearSearchHistory } from '../api/search';
import { getIngredientList, getTagList } from '../api/recipe';
import RecipeCard from '../components/RecipeCard';
import TagChip from '../components/TagChip';
import Empty from '../components/Empty';
import { CardSkeleton, Spinner } from '../components/Loading';
import useAuthStore from '../store/useAuthStore';
import { toast } from '../store/useToastStore';
import { CUISINES } from '../utils/constants';

const PAGE_SIZE = 10;

/** 三种智能模式 Tab（关键词模式为默认隐含模式） */
const MODES = [
  { key: 'fridge', label: '🧊 冰箱清理', searchType: 2, desc: '选出冰箱里现有的食材，匹配能做的菜' },
  { key: 'nutrition', label: '🎯 营养目标', searchType: 3, desc: '拖动滑块设定单份最大热量' },
  { key: 'crowd', label: '👥 人群标签', searchType: 4, desc: '按适宜人群快速找菜' },
];

/**
 * 智能搜索页：关键词 + 冰箱清理 / 营养目标 / 人群标签三模式
 */
export default function Search() {
  const [params] = useSearchParams();
  const token = useAuthStore((s) => s.token);

  const [keyword, setKeyword] = useState(params.get('keyword') || '');
  const [mode, setMode] = useState(''); // '' | fridge | nutrition | crowd
  const [selectedIngredients, setSelectedIngredients] = useState([]);
  const [maxCalories, setMaxCalories] = useState(800);
  const [selectedTag, setSelectedTag] = useState('');
  const cuisineType = params.get('cuisineType') || '';

  const [ingredients, setIngredients] = useState([]); // 食材库
  const [crowdTags, setCrowdTags] = useState([]); // 人群标签字典
  const [history, setHistory] = useState([]);

  const [result, setResult] = useState({ list: [], total: 0 });
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [searched, setSearched] = useState(false);
  const inputRef = useRef(null);

  /** 食材按 category 分组 */
  const groupedIngredients = useMemo(() => {
    const groups = {};
    for (const ing of ingredients) {
      const cat = ing.category || '其他';
      (groups[cat] = groups[cat] || []).push(ing);
    }
    return groups;
  }, [ingredients]);

  // 初始化：拉取食材库 / 人群标签 / 搜索历史
  useEffect(() => {
    getIngredientList().then((d) => setIngredients(d || [])).catch(() => {});
    getTagList()
      .then((d) => setCrowdTags((d || []).filter((t) => t.tagType === 1)))
      .catch(() => {});
    if (token) {
      getSearchHistory().then((d) => setHistory(d || [])).catch(() => {});
    }
  }, [token]);

  /** 组装搜索参数 */
  const buildParams = (p) => {
    const q = { page: p, size: PAGE_SIZE };
    if (keyword.trim()) q.keyword = keyword.trim();
    if (cuisineType) q.cuisineType = cuisineType;
    if (mode === 'fridge' && selectedIngredients.length) {
      q.ingredients = selectedIngredients.join(',');
      q.searchType = 2;
    } else if (mode === 'nutrition') {
      q.maxCalories = maxCalories;
      q.searchType = 3;
    } else if (mode === 'crowd' && selectedTag) {
      q.tagName = selectedTag;
      q.searchType = 4;
    } else {
      q.searchType = 1;
    }
    return q;
  };

  /** 执行搜索（p=1 重置；p>1 加载更多） */
  const doSearch = async (p = 1) => {
    if (p === 1) setLoading(true);
    else setLoadingMore(true);
    try {
      const data = await searchRecipes(buildParams(p));
      setResult((prev) => ({
        total: data?.total || 0,
        list: p === 1 ? data?.list || [] : [...prev.list, ...(data?.list || [])],
      }));
      setPage(p);
      setSearched(true);
      // 登录用户搜索后刷新历史
      if (p === 1 && token && keyword.trim()) {
        getSearchHistory().then((d) => setHistory(d || [])).catch(() => {});
      }
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  // 从首页带参（热搜词 / 菜系）进入时自动搜索
  useEffect(() => {
    if (params.get('keyword') || params.get('cuisineType')) doSearch(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const toggleIngredient = (name) =>
    setSelectedIngredients((arr) =>
      arr.includes(name) ? arr.filter((n) => n !== name) : [...arr, name]
    );

  const hasMore = result.list.length < result.total;
  const cuisineName = params.get('cuisineName') || CUISINES.find((c) => String(c.type) === cuisineType)?.name;

  return (
    <div className="max-w-5xl mx-auto px-4 pt-5 md:pt-8 space-y-5">
      {/* 关键词输入 */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          doSearch(1);
        }}
        className="card flex items-center gap-2 px-4 py-2.5"
      >
        <span>🔍</span>
        <input
          ref={inputRef}
          className="flex-1 bg-transparent text-sm placeholder:text-mute/70"
          placeholder="输入菜名 / 食材，回车搜索"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        {keyword && (
          <button type="button" className="text-mute text-xs" onClick={() => setKeyword('')}>
            ✕
          </button>
        )}
        <button type="submit" className="btn-primary px-4 py-1.5 text-sm shrink-0">
          搜索
        </button>
      </form>

      {cuisineName && (
        <p className="text-xs text-mute">
          当前筛选菜系：<span className="text-cinnabar font-serif">{cuisineName}</span>
        </p>
      )}

      {/* 三模式 Tab */}
      <div className="flex gap-2">
        {MODES.map((m) => (
          <button
            key={m.key}
            onClick={() => setMode(mode === m.key ? '' : m.key)}
            className={`flex-1 py-2.5 rounded-2xl text-xs md:text-sm transition active:scale-95 ${
              mode === m.key ? 'bg-cinnabar text-white shadow-seal' : 'card text-ink/70'
            }`}
          >
            {m.label}
          </button>
        ))}
      </div>

      {/* ===== 冰箱清理模式：食材多选 chips（按 category 分组） ===== */}
      {mode === 'fridge' && (
        <div className="card p-4 space-y-3 animate-fade-up">
          <p className="text-xs text-mute">{MODES[0].desc}</p>
          {Object.keys(groupedIngredients).length === 0 ? (
            <Spinner text="食材库加载中…" />
          ) : (
            Object.entries(groupedIngredients).map(([cat, list]) => (
              <div key={cat}>
                <p className="font-serif text-sm text-ochre mb-1.5">{cat}</p>
                <div className="flex flex-wrap gap-1.5">
                  {list.map((ing) => (
                    <TagChip
                      key={ing.id}
                      tone="ochre"
                      active={selectedIngredients.includes(ing.name)}
                      onClick={() => toggleIngredient(ing.name)}
                    >
                      {ing.name}
                    </TagChip>
                  ))}
                </div>
              </div>
            ))
          )}
          {selectedIngredients.length > 0 && (
            <div className="flex items-center justify-between pt-2 border-t border-ink/5">
              <span className="text-xs text-jade">已选 {selectedIngredients.length} 种食材</span>
              <button className="btn-primary px-5 py-1.5 text-sm" onClick={() => doSearch(1)}>
                匹配菜谱
              </button>
            </div>
          )}
        </div>
      )}

      {/* ===== 营养目标模式：最大热量滑块 ===== */}
      {mode === 'nutrition' && (
        <div className="card p-4 space-y-4 animate-fade-up">
          <p className="text-xs text-mute">{MODES[1].desc}</p>
          <div className="text-center">
            <span className="font-serif text-3xl font-bold text-warmth">{maxCalories}</span>
            <span className="text-xs text-mute ml-1">kcal 以下</span>
          </div>
          <input
            type="range"
            min={200}
            max={1500}
            step={50}
            value={maxCalories}
            onChange={(e) => setMaxCalories(Number(e.target.value))}
            className="w-full accent-cinnabar"
          />
          <div className="flex justify-between text-[10px] text-mute">
            <span>200 轻食</span>
            <span>800 常规</span>
            <span>1500 放纵</span>
          </div>
          <button className="btn-primary w-full py-2.5 text-sm" onClick={() => doSearch(1)}>
            找 {maxCalories} kcal 以下的菜
          </button>
        </div>
      )}

      {/* ===== 人群标签模式 ===== */}
      {mode === 'crowd' && (
        <div className="card p-4 space-y-3 animate-fade-up">
          <p className="text-xs text-mute">{MODES[2].desc}</p>
          <div className="flex flex-wrap gap-2">
            {crowdTags.length === 0 ? (
              <Spinner text="标签加载中…" />
            ) : (
              crowdTags.map((t) => (
                <TagChip
                  key={t.id}
                  tone="jade"
                  active={selectedTag === t.tagName}
                  onClick={() => {
                    setSelectedTag(t.tagName);
                  }}
                >
                  {t.tagName}
                </TagChip>
              ))
            )}
          </div>
          {selectedTag && (
            <button className="btn-primary w-full py-2.5 text-sm" onClick={() => doSearch(1)}>
              查看「{selectedTag}」适宜菜谱
            </button>
          )}
        </div>
      )}

      {/* 搜索历史（登录用户） */}
      {token && !searched && history.length > 0 && (
        <section className="space-y-2">
          <div className="flex items-center justify-between">
            <h3 className="font-serif text-sm font-semibold text-ink/80">最近搜过</h3>
            <button
              className="text-xs text-mute underline underline-offset-2"
              onClick={async () => {
                try {
                  await clearSearchHistory();
                  setHistory([]);
                  toast.success('搜索历史已清空');
                } catch (err) {
                  toast.error(err.message);
                }
              }}
            >
              清空
            </button>
          </div>
          <div className="flex flex-wrap gap-1.5">
            {history.map((h) => (
              <TagChip
                key={h.id}
                tone="mute"
                onClick={() => {
                  setKeyword(h.keyword);
                  setTimeout(() => doSearch(1), 0);
                }}
              >
                {h.keyword}
              </TagChip>
            ))}
          </div>
        </section>
      )}

      {/* ===== 结果区 ===== */}
      {loading ? (
        <CardSkeleton count={6} />
      ) : searched ? (
        result.list.length ? (
          <section className="space-y-4">
            <p className="text-xs text-mute">为你找到 {result.total} 道菜</p>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-3 md:gap-4">
              {result.list.map((r) => (
                <RecipeCard key={r.id} recipe={r} />
              ))}
            </div>
            {hasMore &&
              (loadingMore ? (
                <Spinner />
              ) : (
                <button className="btn-ghost w-full py-2.5 text-sm" onClick={() => doSearch(page + 1)}>
                  加载更多（{result.list.length}/{result.total}）
                </button>
              ))}
          </section>
        ) : (
          <Empty emoji="🍜" text="没有找到匹配的菜谱，换个条件试试" />
        )
      ) : (
        <Empty emoji="🥬" text="输入关键词，或试试上方三种智能找菜模式" />
      )}
    </div>
  );
}

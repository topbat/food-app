import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPostList, toggleLike, getCommentList, addComment } from '../api/social';
import Empty from '../components/Empty';
import SmartImage from '../components/SmartImage';
import { CardSkeleton, Spinner } from '../components/Loading';
import useAuthStore from '../store/useAuthStore';
import { toast } from '../store/useToastStore';
import { POST_TYPES } from '../utils/constants';

const PAGE_SIZE = 10;
// 与后端 social-service 约定：点赞 targetType=1 帖子；评论 targetType=2 帖子
const LIKE_TARGET_POST = 1;
const COMMENT_TARGET_POST = 2;

/** 头像（小图优先缩略图，加载失败逐级回退原图/占位图） */
function Avatar({ src, size = 'w-8 h-8' }) {
  return <SmartImage src={src} className={`${size} rounded-full object-cover shrink-0`} />;
}

/**
 * 社区作品墙：
 * - 顶部 postType 筛选 Tab（全部 + 各类型）
 * - 网格卡片流（分页触底自动加载更多）
 * - 点赞 toggle、点卡片展开详情浮层（全部图片 + 评论列表 + 发评论）
 * - 右下角悬浮「+」发帖入口
 */
export default function Community() {
  const navigate = useNavigate();
  const token = useAuthStore((s) => s.token);

  const [tab, setTab] = useState(0); // 0=全部，否则为 postType
  const [posts, setPosts] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [likedMap, setLikedMap] = useState({}); // postId -> 是否已点赞（本地 toggle 态）
  const [active, setActive] = useState(null); // 详情浮层中的帖子
  const [comments, setComments] = useState({ list: [], total: 0, page: 1 });
  const [commentText, setCommentText] = useState('');

  const sentinelRef = useRef(null); // 触底加载哨兵
  const loadingRef = useRef(false); // 防止 observer 重复触发

  /** 拉取帖子列表（p=1 重置；p>1 追加） */
  const loadPosts = async (p = 1, type = tab) => {
    if (loadingRef.current) return;
    loadingRef.current = true;
    if (p === 1) setLoading(true);
    else setLoadingMore(true);
    try {
      const params = { page: p, size: PAGE_SIZE };
      if (type) params.postType = type;
      const data = await getPostList(params);
      setTotal(data?.total || 0);
      setPosts((prev) => (p === 1 ? data?.list || [] : [...prev, ...(data?.list || [])]));
      setPage(p);
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
      setLoadingMore(false);
      loadingRef.current = false;
    }
  };

  // 切换 Tab 时重新拉第一页
  useEffect(() => {
    loadPosts(1, tab);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab]);

  // 触底加载更多：IntersectionObserver 监听底部哨兵
  useEffect(() => {
    const el = sentinelRef.current;
    if (!el) return;
    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && posts.length < total && !loadingRef.current) {
        loadPosts(page + 1);
      }
    });
    observer.observe(el);
    return () => observer.disconnect();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [posts.length, total, page]);

  /** 点赞 toggle：未登录引导登录；成功后同步列表与浮层中的计数 */
  const handleLike = async (post, e) => {
    e?.stopPropagation();
    if (!token) {
      toast.error('请先登录');
      navigate(`/login?redirect=${encodeURIComponent('/community')}`);
      return;
    }
    try {
      const res = await toggleLike({ targetType: LIKE_TARGET_POST, targetId: post.id });
      setLikedMap((m) => ({ ...m, [post.id]: res.liked }));
      const patch = (p) => (p.id === post.id ? { ...p, likeCount: res.likeCount } : p);
      setPosts((list) => list.map(patch));
      setActive((a) => (a && a.id === post.id ? { ...a, likeCount: res.likeCount } : a));
    } catch (err) {
      toast.error(err.message);
    }
  };

  /** 打开帖子详情浮层并加载评论 */
  const openDetail = (post) => {
    setActive(post);
    setComments({ list: [], total: 0, page: 1 });
    setCommentText('');
    loadComments(post.id, 1);
  };

  /** 加载帖子评论（p>1 追加） */
  const loadComments = async (postId, p = 1) => {
    try {
      const data = await getCommentList({
        targetType: COMMENT_TARGET_POST,
        targetId: postId,
        page: p,
        size: 10,
      });
      setComments((prev) => ({
        total: data?.total || 0,
        page: p,
        list: p === 1 ? data?.list || [] : [...prev.list, ...(data?.list || [])],
      }));
    } catch {
      /* 评论加载失败静默 */
    }
  };

  /** 发评论（需登录），成功后刷新评论与帖子评论数 */
  const handleComment = async () => {
    if (!token) {
      toast.error('请先登录');
      navigate(`/login?redirect=${encodeURIComponent('/community')}`);
      return;
    }
    if (!commentText.trim()) return toast.error('先写点什么吧');
    try {
      await addComment({
        targetType: COMMENT_TARGET_POST,
        targetId: active.id,
        content: commentText.trim(),
      });
      setCommentText('');
      toast.success('评论成功');
      loadComments(active.id, 1);
      // 本地同步评论数 +1
      setPosts((list) =>
        list.map((p) => (p.id === active.id ? { ...p, commentCount: (p.commentCount || 0) + 1 } : p))
      );
      setActive((a) => (a ? { ...a, commentCount: (a.commentCount || 0) + 1 } : a));
    } catch (err) {
      toast.error(err.message);
    }
  };

  const typeLabel = (v) => POST_TYPES.find((t) => t.value === v)?.label || '';

  return (
    <div className="max-w-5xl mx-auto px-4 pt-5 md:pt-8 space-y-5">
      {/* 标题 + 筛选 Tab */}
      <div className="animate-fade-up">
        <h1 className="font-serif text-2xl md:text-3xl font-bold">人间烟火</h1>
        <p className="text-xs text-mute mt-1">晒装盘 · 记日记 · 一起把日子做成好菜</p>
      </div>
      <div className="flex gap-2 overflow-x-auto no-scrollbar -mx-4 px-4">
        {[{ value: 0, label: '全部' }, ...POST_TYPES].map((t) => (
          <button
            key={t.value}
            onClick={() => setTab(t.value)}
            className={`shrink-0 px-4 py-2 rounded-2xl text-xs md:text-sm transition active:scale-95 ${
              tab === t.value ? 'bg-cinnabar text-white shadow-seal' : 'card text-ink/70'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* ===== 帖子卡片流 ===== */}
      {loading ? (
        <CardSkeleton count={6} />
      ) : posts.length === 0 ? (
        <Empty
          emoji="🍽️"
          text="还没有作品，来发第一帖吧"
          action={
            <button className="btn-primary px-5 py-2 text-sm" onClick={() => navigate('/community/post')}>
              去发帖
            </button>
          }
        />
      ) : (
        <>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-3 md:gap-4">
            {posts.map((post) => (
              <button
                key={post.id}
                onClick={() => openDetail(post)}
                className="card overflow-hidden text-left transition md:hover:-translate-y-1 md:hover:shadow-lift active:scale-[0.98] animate-fade-up"
              >
                {/* 首图 */}
                {post.imageUrls?.length > 0 && (
                  <div className="relative">
                    {/* 作品墙卡片：用缩略图提速，失败回退原图 */}
                    <SmartImage src={post.imageUrls[0]} className="w-full h-36 md:h-44 object-cover" />
                    {post.imageUrls.length > 1 && (
                      <span className="absolute top-2 right-2 bg-scrim/60 text-white text-[10px] px-1.5 py-0.5 rounded-md">
                        🖼 {post.imageUrls.length}
                      </span>
                    )}
                  </div>
                )}
                <div className="p-3 space-y-2">
                  {/* 类型角标 + 内容摘要 */}
                  <p className="text-sm leading-snug line-clamp-2">
                    {typeLabel(post.postType) && (
                      <span className="text-[10px] text-warmth bg-warmth/10 rounded px-1 py-0.5 mr-1 align-middle">
                        {typeLabel(post.postType)}
                      </span>
                    )}
                    {post.content}
                  </p>
                  {/* 作者 + 点赞/评论 */}
                  <div className="flex items-center gap-1.5">
                    <Avatar src={post.avatarUrl} size="w-5 h-5" />
                    <span className="text-[11px] text-mute truncate flex-1">{post.nickname || '食客'}</span>
                    <span
                      role="button"
                      tabIndex={0}
                      onClick={(e) => handleLike(post, e)}
                      onKeyDown={(e) => e.key === 'Enter' && handleLike(post, e)}
                      className={`text-[11px] flex items-center gap-0.5 transition active:scale-110 ${
                        likedMap[post.id] ? 'text-cinnabar' : 'text-mute'
                      }`}
                    >
                      {likedMap[post.id] ? '❤️' : '🤍'} {post.likeCount ?? 0}
                    </span>
                    <span className="text-[11px] text-mute flex items-center gap-0.5">💬 {post.commentCount ?? 0}</span>
                  </div>
                </div>
              </button>
            ))}
          </div>
          {/* 触底加载哨兵 */}
          <div ref={sentinelRef} />
          {loadingMore && <Spinner text="加载更多作品…" />}
          {posts.length >= total && posts.length > 0 && (
            <p className="text-center text-[11px] text-mute pb-2">— 到底啦，去做道菜再来发帖吧 —</p>
          )}
        </>
      )}

      {/* ===== 右下角悬浮发帖按钮 ===== */}
      <button
        onClick={() => navigate('/community/post')}
        aria-label="发帖打卡"
        className="fixed bottom-20 md:bottom-10 right-5 md:right-10 z-30 w-14 h-14 rounded-full bg-cinnabar text-white text-2xl shadow-seal flex items-center justify-center active:scale-95 transition animate-pop"
      >
        ＋
      </button>

      {/* ===== 帖子详情浮层：全部图片 + 评论 ===== */}
      {active && (
        <div
          className="fixed inset-0 z-50 bg-scrim/50 flex items-end md:items-center justify-center"
          onClick={() => setActive(null)}
        >
          <div
            className="bg-card w-full md:max-w-lg rounded-t-3xl md:rounded-3xl max-h-[88vh] overflow-y-auto animate-slide-up md:animate-pop"
            onClick={(e) => e.stopPropagation()}
          >
            {/* 作者栏 */}
            <div className="sticky top-0 bg-card/95 backdrop-blur px-5 py-3.5 flex items-center gap-3 border-b border-ink/5 z-10">
              <Avatar src={active.avatarUrl} size="w-9 h-9" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{active.nickname || '食客'}</p>
                <p className="text-[10px] text-mute">
                  {(active.createdAt || '').slice(0, 16).replace('T', ' ')} · {typeLabel(active.postType)}
                </p>
              </div>
              <button
                onClick={() => setActive(null)}
                aria-label="关闭"
                className="w-8 h-8 rounded-full bg-ink/5 text-mute flex items-center justify-center active:scale-95"
              >
                ✕
              </button>
            </div>

            <div className="px-5 py-4 space-y-4">
              {/* 正文 */}
              <p className="text-sm leading-relaxed whitespace-pre-wrap">{active.content}</p>
              {/* 全部图片：详情浮层看原图（thumb=false） */}
              {active.imageUrls?.map((url, i) => (
                <SmartImage key={i} src={url} thumb={false} className="w-full rounded-2xl object-cover" />
              ))}
              {/* 关联菜谱跳转 */}
              {active.recipeId && (
                <button
                  className="w-full bg-paper rounded-xl px-4 py-2.5 text-xs text-ochre flex items-center justify-between active:scale-[0.99]"
                  onClick={() => navigate(`/recipe/${active.recipeId}`)}
                >
                  <span>🍳 看这道菜的做法</span>
                  <span>→</span>
                </button>
              )}
              {/* 点赞 */}
              <button
                onClick={(e) => handleLike(active, e)}
                className={`flex items-center gap-1.5 text-sm transition active:scale-105 ${
                  likedMap[active.id] ? 'text-cinnabar' : 'text-mute'
                }`}
              >
                {likedMap[active.id] ? '❤️' : '🤍'} {active.likeCount ?? 0} 人觉得很赞
              </button>

              {/* 评论区 */}
              <div className="space-y-3 pt-1 border-t border-ink/5">
                <h3 className="font-serif text-sm font-semibold pt-3">评论 {comments.total > 0 && `(${comments.total})`}</h3>
                <div className="flex gap-2">
                  <input
                    className="input-base flex-1"
                    placeholder={token ? '夸夸这道菜…' : '登录后参与讨论'}
                    value={commentText}
                    onChange={(e) => setCommentText(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleComment()}
                  />
                  <button className="btn-primary px-4 text-sm shrink-0" onClick={handleComment}>
                    发送
                  </button>
                </div>
                {comments.list.length === 0 ? (
                  <p className="text-xs text-mute text-center py-3">还没有评论，来抢沙发</p>
                ) : (
                  <ul className="space-y-3 pb-4">
                    {comments.list.map((c) => (
                      <li key={c.id} className="flex gap-2.5">
                        <Avatar src={c.avatarUrl} size="w-7 h-7" />
                        <div className="min-w-0">
                          <p className="text-[10px] text-mute">
                            {c.nickname || '食客'} · {(c.createdAt || '').slice(0, 10)}
                          </p>
                          <p className="text-sm mt-0.5 leading-relaxed">{c.content}</p>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
                {comments.list.length < comments.total && (
                  <button
                    className="btn-ghost w-full py-2 text-xs mb-4"
                    onClick={() => loadComments(active.id, comments.page + 1)}
                  >
                    查看更多评论
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { getProfile, updateProfile, addTag, deleteTag, getDietDaily, getDietMonth } from '../api/user';
import { getMyCheckin, getMyBadges } from '../api/social';
import CalorieRing from '../components/CalorieRing';
import TagChip from '../components/TagChip';
import { PageLoading } from '../components/Loading';
import useAuthStore from '../store/useAuthStore';
import { toast } from '../store/useToastStore';
import { HEALTH_GOALS, MEAL_TYPES, PLACEHOLDER_IMG } from '../utils/constants';

/** 当前年月 'YYYY-MM' */
function currentMonth() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}

/** 今天 'YYYY-MM-DD' */
function todayStr() {
  const d = new Date();
  return `${currentMonth()}-${String(d.getDate()).padStart(2, '0')}`;
}

/**
 * 生成某月的日历格子：前导空位（周一为一周起点）+ 每天的日期串
 * @param month 'YYYY-MM'
 * @returns {blanks:number, days:['YYYY-MM-DD', ...]}
 */
function buildCalendar(month) {
  const [y, m] = month.split('-').map(Number);
  const first = new Date(y, m - 1, 1);
  const daysInMonth = new Date(y, m, 0).getDate();
  const blanks = (first.getDay() + 6) % 7; // 周一=0
  const days = Array.from({ length: daysInMonth }, (_, i) => `${month}-${String(i + 1).padStart(2, '0')}`);
  return { blanks, days };
}

/** 月份加减：offset=±1 */
function shiftMonth(month, offset) {
  const [y, m] = month.split('-').map(Number);
  const d = new Date(y, m - 1 + offset, 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}

// 用户标签类型选项（与 user-service 约定：1 基础标签 2 状态标签）
const USER_TAG_TYPES = [
  { value: 1, label: '基础', tone: 'jade' },
  { value: 2, label: '状态', tone: 'warmth' },
];

/**
 * 个人中心：
 * - 未登录：引导登录卡片
 * - 已登录：头像昵称 / 健康档案（可编辑）/ 我的标签增删 /
 *   饮食月历（超标标红 + 当日明细热量环）/ 打卡与徽章墙 / UGC 与退出入口
 */
export default function Profile() {
  const navigate = useNavigate();
  const token = useAuthStore((s) => s.token);
  const logout = useAuthStore((s) => s.logout);
  const setUser = useAuthStore((s) => s.setUser);

  const [data, setData] = useState(null); // {user, profile, tags}
  const [editing, setEditing] = useState(false); // 健康档案编辑态
  const [form, setForm] = useState({}); // 编辑表单
  const [saving, setSaving] = useState(false);
  const [newTag, setNewTag] = useState('');
  const [newTagType, setNewTagType] = useState(1);

  // 饮食日历
  const [month, setMonth] = useState(currentMonth());
  const [monthData, setMonthData] = useState([]); // [{date,totalCalories}]
  const [selectedDate, setSelectedDate] = useState(todayStr());
  const [daily, setDaily] = useState(null); // 当日明细

  // 打卡与徽章
  const [checkinInfo, setCheckinInfo] = useState({ dates: [], continuousDays: 0 });
  const [badges, setBadges] = useState([]);

  /** 拉取个人主页（user + profile + tags） */
  const loadProfile = () =>
    getProfile()
      .then((d) => {
        setData(d);
        // 同步昵称头像到全局登录态（顶部导航展示）
        if (d?.user) setUser(d.user);
      })
      .catch(() => {});

  // 初始化：已登录才拉数据
  useEffect(() => {
    if (!token) return;
    loadProfile();
    getMyBadges().then((d) => setBadges(d || [])).catch(() => {});
    loadDaily(todayStr());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  // 月份变化：拉饮食月历 + 当月打卡
  useEffect(() => {
    if (!token) return;
    getDietMonth(month).then((d) => setMonthData(d || [])).catch(() => {});
    getMyCheckin(month)
      .then((d) => setCheckinInfo(d || { dates: [], continuousDays: 0 }))
      .catch(() => {});
  }, [token, month]);

  /** 加载某天饮食明细（热量环 + 三大营养素 + 记录列表） */
  const loadDaily = (date) => {
    setSelectedDate(date);
    getDietDaily(date)
      .then(setDaily)
      .catch(() => setDaily(null));
  };

  // ===== 未登录引导 =====
  if (!token) {
    return (
      <div className="max-w-md mx-auto px-4 pt-16 text-center space-y-5">
        <span className="seal-badge w-16 h-16 text-3xl mx-auto animate-pop">食</span>
        <h1 className="font-serif text-2xl font-bold">登录解锁你的美食档案</h1>
        <p className="text-sm text-mute leading-relaxed">
          健康档案 · 饮食日历 · 连续打卡 · 成就徽章
          <br />
          都在等你回来
        </p>
        <Link to={`/login?redirect=${encodeURIComponent('/profile')}`} className="btn-primary inline-block px-10 py-3 font-serif">
          去登录 / 注册
        </Link>
      </div>
    );
  }

  if (!data) return <PageLoading />;
  const { user = {}, profile = {}, tags = [] } = data;
  const calorieTarget = profile?.dailyCalorieTarget || 0;

  /** 进入编辑态：用当前档案填充表单 */
  const startEdit = () => {
    setForm({
      nickname: user.nickname || '',
      heightCm: profile?.heightCm || '',
      weightKg: profile?.weightKg || '',
      allergyHistory: profile?.allergyHistory || '',
      dietPreference: profile?.dietPreference || '',
      healthGoal: profile?.healthGoal || '',
      dailyCalorieTarget: profile?.dailyCalorieTarget || '',
    });
    setEditing(true);
  };

  /** 保存健康档案：仅提交有值字段 */
  const saveProfile = async () => {
    setSaving(true);
    try {
      const payload = {};
      Object.entries(form).forEach(([k, v]) => {
        if (v !== '' && v !== null && v !== undefined) {
          payload[k] = ['heightCm', 'weightKg', 'dailyCalorieTarget'].includes(k) ? Number(v) : v;
        }
      });
      await updateProfile(payload);
      toast.success('档案已更新');
      setEditing(false);
      loadProfile();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setSaving(false);
    }
  };

  /** 添加标签 */
  const handleAddTag = async () => {
    if (!newTag.trim()) return toast.error('输入标签名称');
    try {
      await addTag({ tagName: newTag.trim(), tagType: newTagType });
      setNewTag('');
      toast.success('标签已添加');
      loadProfile();
    } catch (err) {
      toast.error(err.message);
    }
  };

  /** 删除标签 */
  const handleDeleteTag = async (id) => {
    try {
      await deleteTag(id);
      loadProfile();
    } catch (err) {
      toast.error(err.message);
    }
  };

  /** 退出登录：清空 authStore 回首页 */
  const handleLogout = () => {
    if (!window.confirm('确定退出登录吗？')) return;
    logout();
    toast.success('已退出登录');
    navigate('/', { replace: true });
  };

  const { blanks, days } = buildCalendar(month);
  /** date -> totalCalories 映射，渲染日历格用 */
  const calorieByDate = Object.fromEntries((monthData || []).map((d) => [d.date, d.totalCalories]));
  const checkinSet = new Set(checkinInfo.dates || []);

  return (
    <div className="max-w-3xl mx-auto px-4 pt-5 md:pt-8 space-y-5 pb-10">
      {/* ===== 头部：头像昵称 ===== */}
      <div className="flex items-center gap-4 animate-fade-up">
        <img
          src={user.avatarUrl || PLACEHOLDER_IMG}
          onError={(e) => (e.currentTarget.src = PLACEHOLDER_IMG)}
          alt=""
          className="w-16 h-16 rounded-full object-cover bg-ink/5 border-2 border-card shadow-soft"
        />
        <div className="flex-1 min-w-0">
          <h1 className="font-serif text-xl font-bold truncate">{user.nickname || user.username}</h1>
          <p className="text-xs text-mute mt-0.5">
            连续打卡 <b className="text-cinnabar">{checkinInfo.continuousDays || 0}</b> 天 · 徽章{' '}
            <b className="text-warmth">{badges.filter((b) => b.obtained).length}</b> 枚
          </p>
        </div>
        <button className="btn-ghost px-4 py-2 text-xs shrink-0" onClick={handleLogout}>
          退出登录
        </button>
      </div>

      {/* ===== 健康档案卡（查看 / 编辑） ===== */}
      <section className="card p-4 space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="font-serif text-lg font-semibold">健康档案</h2>
          {!editing && (
            <button className="seal-badge px-3 py-1 text-xs active:scale-95" onClick={startEdit}>
              ✎ 编辑
            </button>
          )}
        </div>

        {!editing ? (
          /* 查看态：两列信息 */
          <div className="grid grid-cols-2 gap-x-4 gap-y-2.5 text-sm">
            {[
              ['身高', profile?.heightCm ? `${profile.heightCm} cm` : '—'],
              ['体重', profile?.weightKg ? `${profile.weightKg} kg` : '—'],
              ['过敏史', profile?.allergyHistory || '无'],
              ['饮食偏好', profile?.dietPreference || '—'],
              ['健康目标', profile?.healthGoal || '—'],
              ['每日热量目标', calorieTarget ? `${calorieTarget} kcal` : '—'],
            ].map(([label, val]) => (
              <div key={label}>
                <p className="text-[11px] text-mute">{label}</p>
                <p className="mt-0.5">{val}</p>
              </div>
            ))}
          </div>
        ) : (
          /* 编辑态表单 */
          <div className="space-y-3 animate-fade-up">
            <div className="grid grid-cols-2 gap-3">
              <label className="block">
                <span className="text-[11px] text-mute">昵称</span>
                <input className="input-base mt-1" value={form.nickname} onChange={(e) => setForm((f) => ({ ...f, nickname: e.target.value }))} />
              </label>
              <label className="block">
                <span className="text-[11px] text-mute">每日热量目标 (kcal)</span>
                <input className="input-base mt-1" type="number" value={form.dailyCalorieTarget} onChange={(e) => setForm((f) => ({ ...f, dailyCalorieTarget: e.target.value }))} />
              </label>
              <label className="block">
                <span className="text-[11px] text-mute">身高 (cm)</span>
                <input className="input-base mt-1" type="number" value={form.heightCm} onChange={(e) => setForm((f) => ({ ...f, heightCm: e.target.value }))} />
              </label>
              <label className="block">
                <span className="text-[11px] text-mute">体重 (kg)</span>
                <input className="input-base mt-1" type="number" value={form.weightKg} onChange={(e) => setForm((f) => ({ ...f, weightKg: e.target.value }))} />
              </label>
            </div>
            <label className="block">
              <span className="text-[11px] text-mute">过敏史</span>
              <input className="input-base mt-1" placeholder="如：花生、海鲜" value={form.allergyHistory} onChange={(e) => setForm((f) => ({ ...f, allergyHistory: e.target.value }))} />
            </label>
            <label className="block">
              <span className="text-[11px] text-mute">饮食偏好</span>
              <input className="input-base mt-1" placeholder="如：少辣、偏清淡" value={form.dietPreference} onChange={(e) => setForm((f) => ({ ...f, dietPreference: e.target.value }))} />
            </label>
            <div>
              <span className="text-[11px] text-mute">健康目标</span>
              <div className="flex flex-wrap gap-1.5 mt-1.5">
                {HEALTH_GOALS.map((g) => (
                  <TagChip key={g} tone="jade" active={form.healthGoal === g} onClick={() => setForm((f) => ({ ...f, healthGoal: g }))}>
                    {g}
                  </TagChip>
                ))}
              </div>
            </div>
            <div className="flex gap-3 pt-1">
              <button className="btn-ghost flex-1 py-2.5 text-sm" onClick={() => setEditing(false)}>
                取消
              </button>
              <button className="btn-primary flex-1 py-2.5 text-sm" disabled={saving} onClick={saveProfile}>
                {saving ? '保存中…' : '保存档案'}
              </button>
            </div>
          </div>
        )}
      </section>

      {/* ===== 我的标签（增删） ===== */}
      <section className="card p-4 space-y-3">
        <h2 className="font-serif text-lg font-semibold">我的标签</h2>
        <div className="flex flex-wrap gap-1.5">
          {tags.length === 0 && <p className="text-xs text-mute">添加「减脂期」「熬夜党」等标签，推荐会更懂你</p>}
          {tags.map((t) => (
            <TagChip key={t.id} tone={t.tagType === 2 ? 'warmth' : 'jade'}>
              {t.tagName}
              <button
                aria-label={`删除标签${t.tagName}`}
                className="ml-0.5 opacity-60 hover:opacity-100"
                onClick={() => handleDeleteTag(t.id)}
              >
                ✕
              </button>
            </TagChip>
          ))}
        </div>
        <div className="flex gap-2 items-center">
          {/* 标签类型切换 */}
          <div className="flex rounded-xl bg-paper p-0.5 shrink-0">
            {USER_TAG_TYPES.map((t) => (
              <button
                key={t.value}
                onClick={() => setNewTagType(t.value)}
                className={`px-2.5 py-1.5 rounded-lg text-[11px] transition ${
                  newTagType === t.value ? 'bg-cinnabar text-white' : 'text-mute'
                }`}
              >
                {t.label}
              </button>
            ))}
          </div>
          <input
            className="input-base flex-1"
            placeholder="如：减脂期 / 健身后"
            value={newTag}
            maxLength={10}
            onChange={(e) => setNewTag(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleAddTag()}
          />
          <button className="btn-primary px-4 py-2 text-sm shrink-0" onClick={handleAddTag}>
            添加
          </button>
        </div>
      </section>

      {/* ===== 饮食日历（月视图：总热量 + 超标标红 + 打卡点） ===== */}
      <section className="card p-4 space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="font-serif text-lg font-semibold">饮食日历</h2>
          <div className="flex items-center gap-2 text-sm">
            <button className="w-7 h-7 rounded-full bg-ink/5 active:scale-95" onClick={() => setMonth((m) => shiftMonth(m, -1))} aria-label="上个月">
              ‹
            </button>
            <span className="font-serif text-sm w-20 text-center">{month}</span>
            <button className="w-7 h-7 rounded-full bg-ink/5 active:scale-95" onClick={() => setMonth((m) => shiftMonth(m, 1))} aria-label="下个月">
              ›
            </button>
          </div>
        </div>

        {/* 星期表头（周一起） */}
        <div className="grid grid-cols-7 text-center text-[10px] text-mute">
          {['一', '二', '三', '四', '五', '六', '日'].map((w) => (
            <span key={w}>{w}</span>
          ))}
        </div>
        {/* 日历格：每天显示日期 + 总热量（超标朱砂红）+ 打卡圆点 */}
        <div className="grid grid-cols-7 gap-1">
          {Array.from({ length: blanks }).map((_, i) => (
            <span key={`b${i}`} />
          ))}
          {days.map((date) => {
            const cal = calorieByDate[date];
            const exceed = calorieTarget > 0 && cal > calorieTarget; // 超标判断
            const selected = selectedDate === date;
            return (
              <button
                key={date}
                onClick={() => loadDaily(date)}
                className={`rounded-xl py-1.5 flex flex-col items-center gap-0.5 transition active:scale-95 ${
                  selected ? 'bg-cinnabar text-white shadow-seal' : date === todayStr() ? 'bg-warmth/10' : 'hover:bg-ink/5'
                }`}
              >
                <span className={`text-xs ${selected ? 'text-white' : 'text-ink/80'}`}>{Number(date.slice(8))}</span>
                {cal != null ? (
                  <span className={`text-[9px] leading-none ${selected ? 'text-white/90' : exceed ? 'text-cinnabar font-semibold' : 'text-jade'}`}>
                    {cal}
                  </span>
                ) : (
                  <span className="text-[9px] leading-none opacity-0">0</span>
                )}
                {/* 打卡圆点 */}
                <span className={`w-1 h-1 rounded-full ${checkinSet.has(date) ? (selected ? 'bg-white' : 'bg-cinnabar') : 'opacity-0 bg-mute'}`} />
              </button>
            );
          })}
        </div>

        {/* 当日明细：热量环 + 三大营养素 + 记录列表 */}
        {daily && (
          <div className="bg-paper rounded-2xl p-4 mt-1 space-y-3 animate-fade-up">
            <p className="text-xs text-mute">{selectedDate} 摄入明细</p>
            <div className="flex items-center gap-5">
              <CalorieRing
                value={daily.totalCalories || 0}
                max={daily.calorieTarget || calorieTarget || 2000}
                size={104}
                sub={daily.calorieTarget ? `目标 ${daily.calorieTarget}` : ''}
                over={!!daily.exceedTarget}
              />
              <div className="flex-1 space-y-1.5 text-xs">
                {daily.exceedTarget && <p className="text-cinnabar font-medium">⚠ 今日热量已超标</p>}
                <p className="text-mute">碳水 <b className="text-warmth">{daily.totalCarbs ?? 0}g</b></p>
                <p className="text-mute">蛋白质 <b className="text-jade">{daily.totalProtein ?? 0}g</b></p>
                <p className="text-mute">脂肪 <b className="text-ochre">{daily.totalFat ?? 0}g</b></p>
              </div>
            </div>
            {(daily.records || []).length > 0 ? (
              <ul className="divide-y divide-ink/5">
                {daily.records.map((r) => (
                  <li key={r.id} className="py-2 flex items-center justify-between text-sm">
                    <span className="flex items-center gap-2 min-w-0">
                      <span className="text-[10px] text-warmth bg-warmth/10 rounded px-1.5 py-0.5 shrink-0">
                        {MEAL_TYPES[r.mealType] || '加餐'}
                      </span>
                      <span className="truncate">{r.recipeName}</span>
                    </span>
                    <span className="text-mute text-xs shrink-0">{r.caloriesKcal} kcal</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-xs text-mute text-center py-2">这天还没有记录，去做道菜吧</p>
            )}
          </div>
        )}
      </section>

      {/* ===== 打卡与徽章墙 ===== */}
      <section className="card p-4 space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="font-serif text-lg font-semibold">成就徽章</h2>
          <span className="text-xs text-mute">
            连续打卡 <b className="text-cinnabar font-serif text-base">{checkinInfo.continuousDays || 0}</b> 天
          </span>
        </div>
        {badges.length === 0 ? (
          <p className="text-xs text-mute text-center py-3">完成烹饪并打卡，即可点亮徽章</p>
        ) : (
          <div className="grid grid-cols-4 md:grid-cols-6 gap-3">
            {badges.map((b) => (
              <div
                key={b.id}
                title={b.badgeDesc}
                className={`flex flex-col items-center gap-1 rounded-2xl py-3 transition ${
                  b.obtained ? 'bg-warmth/10' : 'opacity-35 grayscale' // 未获得置灰
                }`}
              >
                <span className="text-2xl">{b.icon || '🏅'}</span>
                <span className="text-[10px] text-ink/70 text-center px-1 leading-tight">{b.badgeName}</span>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* ===== 入口按钮 ===== */}
      <button
        className="btn-primary w-full py-3.5 font-serif text-base shadow-lift"
        onClick={() => navigate('/profile/ugc')}
      >
        📖 上传我的独家菜谱
      </button>
    </div>
  );
}

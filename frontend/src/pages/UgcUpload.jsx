import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { uploadUgcRecipe, getIngredientList, getTagList } from '../api/recipe';
import { isVideoUrl } from '../api/file';
import TagChip from '../components/TagChip';
import SmartImage from '../components/SmartImage';
import UploadButton from '../components/UploadButton';
import { Spinner } from '../components/Loading';
import useAuthStore from '../store/useAuthStore';
import { toast } from '../store/useToastStore';
import { CUISINES, DIFFICULTY, PHASES, FIRE_POWER } from '../utils/constants';

/** 步骤媒体可上传类型：图片 + mp4/mov 视频 */
const STEP_MEDIA_ACCEPT = 'image/jpeg,image/png,image/webp,image/gif,video/mp4,video/quicktime';

/** 向导步骤条配置 */
const WIZARD_STEPS = ['基本信息', '食材清单', '五阶段步骤', '人群标签', '预览提交'];

/** 火候选项（COOK 阶段步骤用，与后端中文值一致） */
const FIRE_OPTIONS = ['大火', '中火', '小火'];

/** 新建一条空步骤 */
const emptyStep = () => ({ actionTitle: '', detail: '', mediaUrl: '', timerSec: '', firePower: '' });

/**
 * UGC 菜谱上传（需登录）：
 * 严格按五步法模板的分步向导表单
 * 基本信息 → 食材清单（实时估算热量）→ 五阶段步骤（每阶段至少 1 条）→ 人群标签 → 预览提交
 * 提交 POST /api/recipe/ugc，成功后提示「已提交审核」并跳回 /profile
 */
export default function UgcUpload() {
  const navigate = useNavigate();
  const token = useAuthStore((s) => s.token);

  const [wizard, setWizard] = useState(0); // 当前向导步骤 0-4
  const [submitting, setSubmitting] = useState(false);

  // ===== 第一步：基本信息 =====
  const [basic, setBasic] = useState({
    title: '',
    coverUrl: '',
    cuisineType: 9, // 默认家常菜
    difficulty: 1,
    totalTimeMin: 30,
    servings: 2,
    description: '',
    tips: '',
  });

  // ===== 第二步：食材 =====
  const [library, setLibrary] = useState([]); // 食材库
  const [ingredients, setIngredients] = useState([]); // 已选 [{ingredientId,name,amount,unit,isEssential,caloriesPer100g}]
  const [pickId, setPickId] = useState(''); // 待添加的食材 id
  const [pickAmount, setPickAmount] = useState('');
  const [pickUnit, setPickUnit] = useState('克');
  const [pickEssential, setPickEssential] = useState(true);

  // ===== 第三步：五阶段步骤（每阶段一个数组） =====
  const [steps, setSteps] = useState({
    PREPARE: [emptyStep()],
    WASH: [emptyStep()],
    CUT: [emptyStep()],
    COOK: [emptyStep()],
    PLATE: [emptyStep()],
  });

  // ===== 第四步：标签 =====
  const [tagDict, setTagDict] = useState([]); // 标签字典
  const [suitableIds, setSuitableIds] = useState([]);
  const [unsuitableIds, setUnsuitableIds] = useState([]);

  // 鉴权页面：未登录跳登录
  useEffect(() => {
    if (!token) {
      toast.error('请先登录');
      navigate(`/login?redirect=${encodeURIComponent('/profile/ugc')}`, { replace: true });
    }
  }, [token, navigate]);

  // 初始化：拉食材库与标签字典
  useEffect(() => {
    getIngredientList().then((d) => setLibrary(d || [])).catch(() => {});
    getTagList().then((d) => setTagDict(d || [])).catch(() => {});
  }, []);

  /** 实时估算总热量：Σ(amount/100 × caloriesPer100g)，按「克/毫升」近似折算 */
  const estimatedCalories = useMemo(
    () =>
      Math.round(
        ingredients.reduce((sum, ing) => sum + ((Number(ing.amount) || 0) / 100) * (ing.caloriesPer100g || 0), 0)
      ),
    [ingredients]
  );

  /** 添加一条食材 */
  const addIngredient = () => {
    const item = library.find((l) => String(l.id) === String(pickId));
    if (!item) return toast.error('请选择食材');
    if (!Number(pickAmount) || Number(pickAmount) <= 0) return toast.error('请填写正确的用量');
    if (ingredients.some((i) => i.ingredientId === item.id)) return toast.error('该食材已添加');
    setIngredients((arr) => [
      ...arr,
      {
        ingredientId: item.id,
        name: item.name,
        amount: Number(pickAmount),
        unit: pickUnit || '克',
        isEssential: pickEssential,
        caloriesPer100g: item.caloriesPer100g || 0,
      },
    ]);
    setPickId('');
    setPickAmount('');
    setPickEssential(true);
  };

  /** 修改某阶段某条步骤的字段 */
  const setStepField = (phase, idx, key, val) =>
    setSteps((s) => ({
      ...s,
      [phase]: s[phase].map((st, i) => (i === idx ? { ...st, [key]: val } : st)),
    }));

  /** 某阶段加一条步骤 */
  const addStepRow = (phase) => setSteps((s) => ({ ...s, [phase]: [...s[phase], emptyStep()] }));

  /** 某阶段删一条步骤（至少保留一条输入行） */
  const removeStepRow = (phase, idx) =>
    setSteps((s) => ({
      ...s,
      [phase]: s[phase].length <= 1 ? [emptyStep()] : s[phase].filter((_, i) => i !== idx),
    }));

  /** 标签双选互斥：同一标签不能既适宜又慎用 */
  const toggleTag = (id, kind) => {
    if (kind === 'suit') {
      setSuitableIds((arr) => (arr.includes(id) ? arr.filter((x) => x !== id) : [...arr, id]));
      setUnsuitableIds((arr) => arr.filter((x) => x !== id));
    } else {
      setUnsuitableIds((arr) => (arr.includes(id) ? arr.filter((x) => x !== id) : [...arr, id]));
      setSuitableIds((arr) => arr.filter((x) => x !== id));
    }
  };

  /** 各阶段有效步骤（标题与详情都已填写） */
  const validStepsOf = (phase) => steps[phase].filter((s) => s.actionTitle.trim() && s.detail.trim());

  /** 分步校验：通过返回 ''，否则返回错误文案 */
  const validateWizard = (idx) => {
    if (idx === 0) {
      if (!basic.title.trim()) return '请填写菜名';
      if (!Number(basic.totalTimeMin) || Number(basic.totalTimeMin) <= 0) return '请填写正确的总耗时';
      if (!Number(basic.servings) || Number(basic.servings) <= 0) return '请填写正确的份数';
      return '';
    }
    if (idx === 1) {
      if (ingredients.length === 0) return '至少添加 1 种食材';
      return '';
    }
    if (idx === 2) {
      // 关键校验：五步法要求 5 个阶段各至少 1 条完整步骤
      for (const ph of PHASES) {
        if (validStepsOf(ph.key).length === 0) return `「${ph.fullName}」阶段至少要有 1 条完整步骤（标题+详情）`;
      }
      return '';
    }
    return '';
  };

  /** 下一步（带当前步校验） */
  const goNext = () => {
    const msg = validateWizard(wizard);
    if (msg) return toast.error(msg);
    setWizard((w) => Math.min(WIZARD_STEPS.length - 1, w + 1));
  };

  /** 组装请求体并提交 */
  const handleSubmit = async () => {
    // 提交前再整体校验一次（防止跳步）
    for (let i = 0; i <= 2; i++) {
      const msg = validateWizard(i);
      if (msg) {
        setWizard(i);
        return toast.error(msg);
      }
    }
    setSubmitting(true);
    try {
      // 步骤平铺：按 PREPARE→PLATE 顺序，各阶段内 stepIndex 从 1 递增
      const flatSteps = [];
      PHASES.forEach((ph) => {
        validStepsOf(ph.key).forEach((st, i) => {
          flatSteps.push({
            phase: ph.key,
            stepIndex: i + 1,
            actionTitle: st.actionTitle.trim(),
            detail: st.detail.trim(),
            mediaUrl: st.mediaUrl.trim() || undefined,
            // 计时与火候仅 COOK 阶段有意义
            timerSec: ph.key === 'COOK' && Number(st.timerSec) > 0 ? Number(st.timerSec) : undefined,
            firePower: ph.key === 'COOK' && st.firePower ? st.firePower : undefined,
          });
        });
      });
      const res = await uploadUgcRecipe({
        title: basic.title.trim(),
        coverUrl: basic.coverUrl.trim() || undefined,
        cuisineType: Number(basic.cuisineType),
        difficulty: Number(basic.difficulty),
        totalTimeMin: Number(basic.totalTimeMin),
        servings: Number(basic.servings),
        description: basic.description.trim() || undefined,
        tips: basic.tips.trim() || undefined,
        ingredients: ingredients.map((i) => ({
          ingredientId: i.ingredientId,
          amount: i.amount,
          unit: i.unit,
          isEssential: i.isEssential,
        })),
        steps: flatSteps,
        suitableTagIds: suitableIds,
        unsuitableTagIds: unsuitableIds,
      });
      toast.success(`已提交审核（编号 ${res?.id ?? ''}），通过后即可被搜索到`);
      navigate('/profile', { replace: true });
    } catch (err) {
      toast.error(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const tagName = (id) => tagDict.find((t) => t.id === id)?.tagName || id;

  return (
    <div className="max-w-3xl mx-auto px-4 pt-5 md:pt-8 space-y-5 pb-10">
      {/* 标题栏 */}
      <div className="flex items-center gap-3 animate-fade-up">
        <button
          onClick={() => navigate(-1)}
          aria-label="返回"
          className="w-9 h-9 rounded-full bg-ink/5 text-ink/60 flex items-center justify-center active:scale-95"
        >
          ←
        </button>
        <div>
          <h1 className="font-serif text-xl md:text-2xl font-bold">上传我的菜谱</h1>
          <p className="text-[11px] text-mute">按「备-洗-切-煮-盘」五步法整理，审核通过后全站可见</p>
        </div>
      </div>

      {/* ===== 向导步骤条 ===== */}
      <div className="flex items-center">
        {WIZARD_STEPS.map((label, i) => (
          <div key={label} className="flex items-center flex-1 last:flex-none">
            <button
              onClick={() => i < wizard && setWizard(i)} // 只允许回退到已完成步骤
              className="flex flex-col items-center gap-1"
            >
              <span
                className={`w-7 h-7 rounded-full text-xs flex items-center justify-center font-serif transition ${
                  i === wizard
                    ? 'bg-cinnabar text-white shadow-seal'
                    : i < wizard
                      ? 'bg-jade/15 text-jade'
                      : 'bg-ink/5 text-mute'
                }`}
              >
                {i < wizard ? '✓' : i + 1}
              </span>
              <span className={`text-[9px] whitespace-nowrap ${i === wizard ? 'text-cinnabar' : 'text-mute'}`}>{label}</span>
            </button>
            {i < WIZARD_STEPS.length - 1 && (
              <div className={`flex-1 h-px mx-1 mb-3.5 ${i < wizard ? 'bg-jade/40' : 'bg-ink/10'}`} />
            )}
          </div>
        ))}
      </div>

      {/* ===== 第 1 步：基本信息 ===== */}
      {wizard === 0 && (
        <section className="card p-4 space-y-3 animate-fade-up">
          <label className="block">
            <span className="text-[11px] text-mute">菜名 *</span>
            <input
              className="input-base mt-1"
              placeholder="如：外婆红烧肉"
              maxLength={30}
              value={basic.title}
              onChange={(e) => setBasic((b) => ({ ...b, title: e.target.value }))}
            />
          </label>
          <label className="block">
            <span className="text-[11px] text-mute">封面图</span>
            <div className="flex gap-2 mt-1 items-center">
              <SmartImage src={basic.coverUrl.trim()} className="w-12 h-12 rounded-xl object-cover shrink-0" />
              {/* 主入口：本地上传封面（bizType=recipe，带进度）；右侧 URL 输入兜底 */}
              <UploadButton
                bizType="recipe"
                onUploaded={(data) => setBasic((b) => ({ ...b, coverUrl: data.url }))}
                className="shrink-0 px-3 py-2.5 rounded-xl text-xs border border-dashed border-ink/15 text-mute hover:border-cinnabar/50 hover:text-cinnabar transition active:scale-95 disabled:opacity-60"
              >
                📷 本地上传
              </UploadButton>
              <input
                className="input-base flex-1"
                placeholder="或粘贴 https://… 图片链接"
                value={basic.coverUrl}
                onChange={(e) => setBasic((b) => ({ ...b, coverUrl: e.target.value }))}
              />
            </div>
          </label>
          <div className="grid grid-cols-2 gap-3">
            <label className="block">
              <span className="text-[11px] text-mute">菜系</span>
              <select
                className="input-base mt-1"
                value={basic.cuisineType}
                onChange={(e) => setBasic((b) => ({ ...b, cuisineType: Number(e.target.value) }))}
              >
                {CUISINES.map((c) => (
                  <option key={c.type} value={c.type}>
                    {c.emoji} {c.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="block">
              <span className="text-[11px] text-mute">难度</span>
              <select
                className="input-base mt-1"
                value={basic.difficulty}
                onChange={(e) => setBasic((b) => ({ ...b, difficulty: Number(e.target.value) }))}
              >
                {Object.entries(DIFFICULTY).map(([v, label]) => (
                  <option key={v} value={v}>
                    {label}
                  </option>
                ))}
              </select>
            </label>
            <label className="block">
              <span className="text-[11px] text-mute">总耗时（分钟）*</span>
              <input
                className="input-base mt-1"
                type="number"
                min={1}
                value={basic.totalTimeMin}
                onChange={(e) => setBasic((b) => ({ ...b, totalTimeMin: e.target.value }))}
              />
            </label>
            <label className="block">
              <span className="text-[11px] text-mute">份数（人份）*</span>
              <input
                className="input-base mt-1"
                type="number"
                min={1}
                value={basic.servings}
                onChange={(e) => setBasic((b) => ({ ...b, servings: e.target.value }))}
              />
            </label>
          </div>
          <label className="block">
            <span className="text-[11px] text-mute">一句话描述</span>
            <input
              className="input-base mt-1"
              placeholder="如：肥而不腻，下饭神器"
              maxLength={100}
              value={basic.description}
              onChange={(e) => setBasic((b) => ({ ...b, description: e.target.value }))}
            />
          </label>
          <label className="block">
            <span className="text-[11px] text-mute">小贴士</span>
            <textarea
              className="input-base mt-1 resize-none"
              rows={2}
              placeholder="独家心得，如：焯水时加两片姜去腥"
              maxLength={200}
              value={basic.tips}
              onChange={(e) => setBasic((b) => ({ ...b, tips: e.target.value }))}
            />
          </label>
        </section>
      )}

      {/* ===== 第 2 步：食材清单（实时估算热量） ===== */}
      {wizard === 1 && (
        <section className="space-y-4 animate-fade-up">
          {/* 估算热量看板 */}
          <div className="card p-4 flex items-center justify-between">
            <div>
              <p className="text-xs text-mute">估算总热量（按每 100g 营养折算）</p>
              <p className="font-serif text-3xl font-bold text-warmth mt-1">
                {estimatedCalories} <span className="text-sm font-sans text-mute">kcal</span>
              </p>
            </div>
            <span className="text-3xl">🔥</span>
          </div>

          {/* 添加食材表单 */}
          <div className="card p-4 space-y-3">
            <h2 className="font-serif text-sm font-semibold">从食材库添加</h2>
            {library.length === 0 ? (
              <Spinner text="食材库加载中…" />
            ) : (
              <>
                <select className="input-base" value={pickId} onChange={(e) => setPickId(e.target.value)}>
                  <option value="">选择食材…</option>
                  {library.map((l) => (
                    <option key={l.id} value={l.id}>
                      {l.name}（{l.caloriesPer100g ?? 0} kcal/100g）
                    </option>
                  ))}
                </select>
                <div className="flex gap-2">
                  <input
                    className="input-base flex-1"
                    type="number"
                    min={1}
                    placeholder="用量"
                    value={pickAmount}
                    onChange={(e) => setPickAmount(e.target.value)}
                  />
                  <select className="input-base w-24 shrink-0" value={pickUnit} onChange={(e) => setPickUnit(e.target.value)}>
                    {['克', '毫升', '个', '勺', '片'].map((u) => (
                      <option key={u}>{u}</option>
                    ))}
                  </select>
                  <label className="flex items-center gap-1 text-xs text-mute shrink-0">
                    <input
                      type="checkbox"
                      className="accent-cinnabar"
                      checked={pickEssential}
                      onChange={(e) => setPickEssential(e.target.checked)}
                    />
                    核心
                  </label>
                </div>
                <button className="btn-primary w-full py-2.5 text-sm" onClick={addIngredient}>
                  ＋ 添加食材
                </button>
              </>
            )}
          </div>

          {/* 已选食材列表 */}
          <div className="card p-4">
            <h2 className="font-serif text-sm font-semibold mb-2">已选食材（{ingredients.length}）</h2>
            {ingredients.length === 0 ? (
              <p className="text-xs text-mute text-center py-3">还没有食材，先从上方添加</p>
            ) : (
              <ul className="divide-y divide-ink/5">
                {ingredients.map((ing) => (
                  <li key={ing.ingredientId} className="flex items-center justify-between py-2 text-sm">
                    <span className="flex items-center gap-2 min-w-0">
                      <span className="truncate">{ing.name}</span>
                      {ing.isEssential && <TagChip tone="cinnabar" className="!text-[10px]">核心</TagChip>}
                    </span>
                    <span className="flex items-center gap-3 shrink-0">
                      <span className="text-mute text-xs">
                        {ing.amount} {ing.unit} ≈ {Math.round((ing.amount / 100) * ing.caloriesPer100g)} kcal
                      </span>
                      <button
                        aria-label="删除食材"
                        className="w-6 h-6 rounded-full bg-ink/5 text-mute text-xs flex items-center justify-center active:scale-95"
                        onClick={() => setIngredients((arr) => arr.filter((i) => i.ingredientId !== ing.ingredientId))}
                      >
                        ✕
                      </button>
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      )}

      {/* ===== 第 3 步：五阶段步骤 ===== */}
      {wizard === 2 && (
        <section className="space-y-3 animate-fade-up">
          <p className="text-xs text-mute">五个阶段各至少 1 条步骤才能提交；「煮」阶段可设置倒计时与火候</p>
          {PHASES.map((ph) => (
            <div key={ph.key} className="card p-4 space-y-3">
              <div className="flex items-center gap-2">
                <span className="seal-badge w-7 h-7 text-xs">{ph.name}</span>
                <h2 className="font-serif text-sm font-semibold">{ph.fullName}</h2>
                <span className={`text-[10px] ${validStepsOf(ph.key).length ? 'text-jade' : 'text-cinnabar'}`}>
                  {validStepsOf(ph.key).length ? `✓ ${validStepsOf(ph.key).length} 条` : '待填写'}
                </span>
                <button
                  className="ml-auto seal-badge px-2.5 py-1 text-[11px] active:scale-95"
                  onClick={() => addStepRow(ph.key)}
                >
                  ＋ 加一条
                </button>
              </div>
              {steps[ph.key].map((st, i) => (
                <div key={i} className="bg-paper rounded-2xl p-3 space-y-2">
                  <div className="flex items-center gap-2">
                    <span className="text-[11px] text-mute shrink-0">第 {i + 1} 条</span>
                    <input
                      className="input-base flex-1"
                      placeholder="动作标题，如：滑炒鸡丁"
                      maxLength={20}
                      value={st.actionTitle}
                      onChange={(e) => setStepField(ph.key, i, 'actionTitle', e.target.value)}
                    />
                    <button
                      aria-label="删除步骤"
                      className="w-7 h-7 rounded-full bg-ink/5 text-mute text-xs flex items-center justify-center active:scale-95 shrink-0"
                      onClick={() => removeStepRow(ph.key, i)}
                    >
                      ✕
                    </button>
                  </div>
                  <textarea
                    className="input-base resize-none"
                    rows={2}
                    placeholder="详细说明，如：热锅冷油，下鸡丁滑散至变色"
                    maxLength={200}
                    value={st.detail}
                    onChange={(e) => setStepField(ph.key, i, 'detail', e.target.value)}
                  />
                  {/* 步骤媒体：本地上传图片/视频为主入口，URL 输入兜底 */}
                  <div className="flex items-center gap-2">
                    {st.mediaUrl.trim() && (
                      // 预览：视频显示首帧缩略图（_thumb.jpg），图片走缩略图回退链
                      <SmartImage
                        src={st.mediaUrl.trim()}
                        thumb
                        className="w-10 h-10 rounded-lg object-cover shrink-0"
                        title={isVideoUrl(st.mediaUrl) ? '视频首帧预览' : '图片预览'}
                      />
                    )}
                    <UploadButton
                      bizType="step"
                      accept={STEP_MEDIA_ACCEPT}
                      onUploaded={(data) => setStepField(ph.key, i, 'mediaUrl', data.url)}
                      className="shrink-0 px-2.5 py-2.5 rounded-xl text-[11px] border border-dashed border-ink/15 text-mute hover:border-cinnabar/50 hover:text-cinnabar transition active:scale-95 disabled:opacity-60"
                    >
                      📷 图片/视频
                    </UploadButton>
                    <input
                      className="input-base flex-1"
                      placeholder="或粘贴媒体链接（可选）"
                      value={st.mediaUrl}
                      onChange={(e) => setStepField(ph.key, i, 'mediaUrl', e.target.value)}
                    />
                  </div>
                  {/* COOK 阶段专属：倒计时秒数 + 火候 */}
                  {ph.key === 'COOK' && (
                    <div className="flex items-center gap-2">
                      <input
                        className="input-base w-32"
                        type="number"
                        min={0}
                        placeholder="计时（秒）"
                        value={st.timerSec}
                        onChange={(e) => setStepField(ph.key, i, 'timerSec', e.target.value)}
                      />
                      <div className="flex gap-1.5">
                        {FIRE_OPTIONS.map((f) => (
                          <TagChip
                            key={f}
                            tone="warmth"
                            active={st.firePower === f}
                            onClick={() => setStepField(ph.key, i, 'firePower', st.firePower === f ? '' : f)}
                          >
                            {FIRE_POWER[f]?.emoji} {f}
                          </TagChip>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          ))}
        </section>
      )}

      {/* ===== 第 4 步：人群标签 ===== */}
      {wizard === 3 && (
        <section className="space-y-4 animate-fade-up">
          <div className="card p-4 space-y-2.5">
            <h2 className="font-serif text-sm font-semibold text-jade">✅ 适宜人群/场景</h2>
            <div className="flex flex-wrap gap-1.5">
              {tagDict.length === 0 ? (
                <Spinner text="标签加载中…" />
              ) : (
                tagDict.map((t) => (
                  <TagChip key={t.id} tone="jade" active={suitableIds.includes(t.id)} onClick={() => toggleTag(t.id, 'suit')}>
                    {t.tagName}
                  </TagChip>
                ))
              )}
            </div>
          </div>
          <div className="card p-4 space-y-2.5">
            <h2 className="font-serif text-sm font-semibold text-cinnabar">❌ 慎用人群</h2>
            <div className="flex flex-wrap gap-1.5">
              {tagDict.map((t) => (
                <TagChip key={t.id} tone="cinnabar" active={unsuitableIds.includes(t.id)} onClick={() => toggleTag(t.id, 'unsuit')}>
                  {t.tagName}
                </TagChip>
              ))}
            </div>
          </div>
          <p className="text-[11px] text-mute text-center">标签可不选，但选了能让推荐更精准</p>
        </section>
      )}

      {/* ===== 第 5 步：预览提交 ===== */}
      {wizard === 4 && (
        <section className="space-y-4 animate-fade-up">
          <div className="card overflow-hidden">
            {/* 预览大图用原图 */}
            <SmartImage src={basic.coverUrl.trim()} thumb={false} className="w-full h-44 object-cover" />
            <div className="p-4 space-y-3">
              <div className="flex items-start justify-between gap-2">
                <h2 className="font-serif text-xl font-bold">{basic.title || '未命名菜谱'}</h2>
                <span className="seal-badge px-2.5 py-1 text-xs shrink-0">
                  {CUISINES.find((c) => c.type === Number(basic.cuisineType))?.name}
                </span>
              </div>
              <p className="text-xs text-mute">
                {DIFFICULTY[basic.difficulty]} · ⏱ {basic.totalTimeMin} 分钟 · {basic.servings} 人份 · 估算{' '}
                <b className="text-warmth">{estimatedCalories}</b> kcal
              </p>
              {basic.description && <p className="text-sm text-ink/70">{basic.description}</p>}
              {/* 食材摘要 */}
              <div>
                <p className="text-xs text-mute mb-1">食材 {ingredients.length} 种</p>
                <p className="text-sm leading-relaxed">
                  {ingredients.map((i) => `${i.name} ${i.amount}${i.unit}`).join('、') || '—'}
                </p>
              </div>
              {/* 五阶段步骤数摘要 */}
              <div className="flex gap-2">
                {PHASES.map((ph) => (
                  <span key={ph.key} className="flex-1 bg-paper rounded-xl py-2 text-center">
                    <span className="block font-serif text-sm">{ph.name}</span>
                    <span className="text-[10px] text-mute">{validStepsOf(ph.key).length} 步</span>
                  </span>
                ))}
              </div>
              {/* 标签摘要 */}
              {(suitableIds.length > 0 || unsuitableIds.length > 0) && (
                <div className="flex flex-wrap gap-1.5">
                  {suitableIds.map((id) => (
                    <TagChip key={`s${id}`} tone="jade">✅ {tagName(id)}</TagChip>
                  ))}
                  {unsuitableIds.map((id) => (
                    <TagChip key={`u${id}`} tone="cinnabar">❌ {tagName(id)}</TagChip>
                  ))}
                </div>
              )}
              {basic.tips && (
                <p className="text-xs text-ochre bg-ochre/5 rounded-xl p-3 leading-relaxed">💡 {basic.tips}</p>
              )}
            </div>
          </div>
          <p className="text-[11px] text-mute text-center">提交后进入审核（status=待审核），审核通过即可被全站搜索到</p>
        </section>
      )}

      {/* ===== 向导底部按钮 ===== */}
      <div className="flex gap-3">
        {wizard > 0 && (
          <button className="btn-ghost flex-1 py-3.5" onClick={() => setWizard((w) => w - 1)}>
            上一步
          </button>
        )}
        {wizard < WIZARD_STEPS.length - 1 ? (
          <button className="btn-primary flex-1 py-3.5 font-serif" onClick={goNext}>
            下一步：{WIZARD_STEPS[wizard + 1]}
          </button>
        ) : (
          <button className="btn-primary flex-1 py-3.5 font-serif shadow-lift" disabled={submitting} onClick={handleSubmit}>
            {submitting ? '提交中…' : '📤 提交审核'}
          </button>
        )}
      </div>
    </div>
  );
}

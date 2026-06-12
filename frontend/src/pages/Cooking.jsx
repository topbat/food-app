import { useEffect, useMemo, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getSession,
  nextStep,
  prevStep,
  abandonSession,
  startTimer,
  getTimers,
  cancelTimer,
  sendVoiceCommand,
} from '../api/kitchen';
import { getRecipeDetail } from '../api/recipe';
import { addDietRecord } from '../api/user';
import useAuthStore from '../store/useAuthStore';
import useCookingStore from '../store/useCookingStore';
import { toast } from '../store/useToastStore';
import { PageLoading } from '../components/Loading';
import { PHASES, FIRE_POWER, inferMealType } from '../utils/constants';

// ===== 会话/计时器状态码（与后端 kitchen-service 约定一致） =====
const SESSION_FINISHED = 2; // 会话已完成
const TIMER_RUNNING = 1; // 计时器运行中

/** 秒数格式化为 mm:ss（厨房倒计时显示） */
function formatSec(sec) {
  const s = Math.max(0, Math.round(sec));
  return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;
}

/** 获取今天的 yyyy-MM-dd（记录饮食用） */
function todayStr() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

/**
 * 计时到点提示音：Web Audio 合成三声清脆「叮」
 * （不依赖音频文件，浏览器不支持时静默）
 */
function playDing() {
  try {
    const Ctx = window.AudioContext || window.webkitAudioContext;
    if (!Ctx) return;
    const ctx = new Ctx();
    [0, 0.35, 0.7].forEach((delay) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.type = 'sine';
      osc.frequency.value = 988; // B5 音高，清脆
      gain.gain.setValueAtTime(0.0001, ctx.currentTime + delay);
      gain.gain.exponentialRampToValueAtTime(0.35, ctx.currentTime + delay + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + delay + 0.3);
      osc.start(ctx.currentTime + delay);
      osc.stop(ctx.currentTime + delay + 0.32);
    });
  } catch {
    /* 音频不可用静默 */
  }
}

/**
 * 厨房模式（全屏沉浸，Layout 之外）：
 * - 顶部整体进度条（progressPercent，权重 备10/洗5/切15/煮60/盘10）+ 五阶段印章指示器
 * - 中部超大字号当前步骤（远距离可读），COOK 阶段显示火候徽标
 * - 多并行倒计时面板（本地每秒递减 + 每 10 秒与后端同步，到点震动+提示音+Toast）
 * - 上一步/下一步大按钮 + 左右滑动手势翻页（跨阶段回退 40900 友好提示）
 * - 语音指令（Web Speech API，不可用降级为文本输入）
 * - 屏幕常亮 wakeLock + 防误触锁定（长按 2 秒解锁）
 * - 完成后成就卡片：热量换算慢跑分钟、拍照打卡、记录饮食日历
 */
export default function Cooking() {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const token = useAuthStore((s) => s.token);

  // 会话与菜谱信息共享到 cooking store（成就卡展示热量用）
  const session = useCookingStore((s) => s.session);
  const setSession = useCookingStore((s) => s.setSession);
  const recipeInfo = useCookingStore((s) => s.recipeInfo);
  const setRecipeInfo = useCookingStore((s) => s.setRecipeInfo);
  const clearCooking = useCookingStore((s) => s.clear);

  const [timers, setTimers] = useState([]); // 本地计时器快照（每秒递减）
  const [locked, setLocked] = useState(false); // 防误触锁定
  const [unlockProgress, setUnlockProgress] = useState(0); // 长按解锁进度 0-100
  const [completed, setCompleted] = useState(false); // 成就卡片浮层
  const [stepping, setStepping] = useState(false); // 翻页请求中（防连点）
  const [listening, setListening] = useState(false); // 语音识别中
  const [voiceInputOpen, setVoiceInputOpen] = useState(false); // 语音降级文本输入
  const [voiceText, setVoiceText] = useState('');

  const completedRef = useRef(false); // 完成流程只触发一次
  const alertedTimerIds = useRef(new Set()); // 已提醒过的计时器，避免重复响铃
  const touchStart = useRef(null); // 触摸起点（滑动手势）
  const unlockTimer = useRef(null); // 长按解锁计时
  const recognitionRef = useRef(null); // SpeechRecognition 实例
  const sessionRef = useRef(null); // 给定时器回调读取最新会话
  sessionRef.current = session;

  // ========== 初始化：拉会话 + 菜谱详情（热量数据） ==========
  useEffect(() => {
    let cancelled = false;
    getSession(sessionId)
      .then((s) => {
        if (cancelled) return;
        setSession(s);
        setTimers(s.timers || []);
        // 进入时已是完成态（如刷新页面），直接展示成就卡
        if (s.status === SESSION_FINISHED) {
          completedRef.current = true;
          setCompleted(true);
        }
        // 拉菜谱详情拿热量/营养（失败静默，成就卡降级展示）
        if (s.recipeId) {
          getRecipeDetail(s.recipeId)
            .then((d) => !cancelled && setRecipeInfo(d?.info || null))
            .catch(() => {});
        }
      })
      .catch((err) => {
        toast.error(err.message);
        navigate('/', { replace: true });
      });
    return () => {
      cancelled = true;
      clearCooking(); // 离开厨房模式清空共享态
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionId]);

  // ========== 屏幕常亮：申请 wakeLock（失败静默），切回前台时重新申请 ==========
  useEffect(() => {
    let lock = null;
    const request = async () => {
      try {
        lock = await navigator.wakeLock?.request('screen');
      } catch {
        /* 不支持或被拒绝时静默降级 */
      }
    };
    request();
    const onVisible = () => {
      if (document.visibilityState === 'visible') request();
    };
    document.addEventListener('visibilitychange', onVisible);
    return () => {
      document.removeEventListener('visibilitychange', onVisible);
      lock?.release?.().catch(() => {});
    };
  }, []);

  // ========== 计时器引擎：本地每秒递减；每 10 秒与后端同步一次 ==========
  useEffect(() => {
    let tick = 0;
    const itv = setInterval(() => {
      tick += 1;
      // 1) 本地递减，检测「刚到点」的计时器触发提醒
      setTimers((prev) =>
        prev.map((t) => {
          if (t.status !== TIMER_RUNNING || t.remainSec <= 0) return t;
          const remain = t.remainSec - 1;
          if (remain <= 0 && !alertedTimerIds.current.has(t.id)) {
            // ===== 计时到点：震动 + 提示音 + Toast 三重提醒 =====
            alertedTimerIds.current.add(t.id);
            try {
              navigator.vibrate?.([300, 120, 300, 120, 300]);
            } catch {
              /* 震动不可用静默 */
            }
            playDing();
            toast.success(`⏰ 「${t.timerName}」时间到啦！`);
          }
          return { ...t, remainSec: Math.max(0, remain) };
        })
      );
      // 2) 定期与后端同步（以后端 remainSec 为准，纠正本地漂移）
      if (tick % 10 === 0 && sessionRef.current && !completedRef.current) {
        getTimers(sessionId)
          .then((list) => setTimers(list || []))
          .catch(() => {});
      }
    }, 1000);
    return () => clearInterval(itv);
  }, [sessionId]);

  // ========== 完成流程：只触发一次，弹成就卡 + 记录饮食日历 ==========
  const triggerComplete = (s) => {
    if (completedRef.current) return;
    completedRef.current = true;
    setCompleted(true);
    try {
      navigator.vibrate?.([200, 100, 200]);
    } catch {
      /* 静默 */
    }
    playDing();
    // 已登录则按当前时间推断餐次，自动写入饮食日历（失败静默不打扰）
    if (token) {
      addDietRecord({
        recordDate: todayStr(),
        mealType: inferMealType(),
        recipeId: s?.recipeId,
        recipeName: s?.recipeName || recipeInfo?.title || '一道好菜',
        caloriesKcal: recipeInfo?.caloriesKcal || 0,
        carbsG: recipeInfo?.carbsG || 0,
        proteinG: recipeInfo?.proteinG || 0,
        fatG: recipeInfo?.fatG || 0,
      }).catch(() => {});
    }
  };

  /** 应用后端返回的最新会话；若状态已完成则进入成就流程 */
  const applySession = (s) => {
    if (!s) return;
    setSession(s);
    if (s.timers) setTimers(s.timers);
    if (s.status === SESSION_FINISHED) triggerComplete(s);
  };

  // ========== 上一步 / 下一步 ==========
  const goNext = async () => {
    if (stepping || completedRef.current) return;
    setStepping(true);
    try {
      const s = await nextStep(sessionId);
      applySession(s);
    } catch (err) {
      toast.error(err.message);
    } finally {
      setStepping(false);
    }
  };

  const goPrev = async () => {
    if (stepping || completedRef.current) return;
    setStepping(true);
    try {
      const s = await prevStep(sessionId);
      applySession(s);
    } catch (err) {
      // 关键交互：跨阶段回退后端返回 40900，给厨房场景友好的提示
      if (err.code === 40900) {
        toast.error('已是本阶段第一步，不能跨阶段回退哦');
      } else {
        toast.error(err.message);
      }
    } finally {
      setStepping(false);
    }
  };

  // ========== 触摸滑动手势：左滑下一步 / 右滑上一步 ==========
  const onTouchStart = (e) => {
    const t = e.touches[0];
    touchStart.current = { x: t.clientX, y: t.clientY };
  };
  const onTouchEnd = (e) => {
    if (!touchStart.current || locked || completed) return;
    const t = e.changedTouches[0];
    const dx = t.clientX - touchStart.current.x;
    const dy = t.clientY - touchStart.current.y;
    touchStart.current = null;
    // 横向位移超过 60px 且大于纵向位移才认定为翻页手势（避免误触滚动）
    if (Math.abs(dx) > 60 && Math.abs(dx) > Math.abs(dy)) {
      if (dx < 0) goNext();
      else goPrev();
    }
  };

  // ========== 开启步骤倒计时 ==========
  const handleStartTimer = async (step) => {
    try {
      const t = await startTimer(sessionId, {
        timerName: step.actionTitle || '步骤计时',
        totalSec: step.timerSec,
      });
      setTimers((prev) => [...prev.filter((x) => x.id !== t.id), t]);
      toast.success(`已开始计时 ${formatSec(step.timerSec)}`);
    } catch (err) {
      toast.error(err.message);
    }
  };

  /** 取消某个计时器 */
  const handleCancelTimer = async (timerId) => {
    try {
      await cancelTimer(timerId);
      setTimers((prev) => prev.filter((t) => t.id !== timerId));
    } catch (err) {
      toast.error(err.message);
    }
  };

  // ========== 语音指令：Web Speech API → 后端解析 ==========
  /** 把识别/输入的文本发给后端解析，按 parsedAction 更新会话 */
  const handleVoiceText = async (text) => {
    const cmd = (text || '').trim();
    if (!cmd) return;
    try {
      const res = await sendVoiceCommand(sessionId, cmd);
      if (res?.session) applySession(res.session); // NEXT/PREV 等指令带回最新会话
      if (res?.message) toast.success(res.message);
    } catch (err) {
      toast.error(err.message);
    }
  };

  /** 点击麦克风：优先 webkitSpeechRecognition，不可用降级为文本输入框 */
  const handleMicClick = () => {
    const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SR) {
      setVoiceInputOpen(true); // 降级：手动输入指令文本
      return;
    }
    if (listening) {
      recognitionRef.current?.stop();
      return;
    }
    const rec = new SR();
    recognitionRef.current = rec;
    rec.lang = 'zh-CN';
    rec.interimResults = false;
    rec.maxAlternatives = 1;
    rec.onstart = () => setListening(true);
    rec.onend = () => setListening(false);
    rec.onerror = () => {
      setListening(false);
      toast.error('没听清，再试一次或点击改用文字输入');
    };
    rec.onresult = (e) => {
      const text = e.results?.[0]?.[0]?.transcript;
      if (text) handleVoiceText(text);
    };
    try {
      rec.start();
    } catch {
      setVoiceInputOpen(true);
    }
  };

  // 卸载时停掉语音识别
  useEffect(() => () => recognitionRef.current?.stop?.(), []);

  // ========== 防误触：长按 2 秒解锁 ==========
  const startUnlockPress = () => {
    const startAt = Date.now();
    // 每 50ms 刷新进度环，满 2000ms 解锁
    unlockTimer.current = setInterval(() => {
      const p = Math.min(100, ((Date.now() - startAt) / 2000) * 100);
      setUnlockProgress(p);
      if (p >= 100) {
        clearInterval(unlockTimer.current);
        setLocked(false);
        setUnlockProgress(0);
        toast.success('已解锁');
      }
    }, 50);
  };
  const cancelUnlockPress = () => {
    clearInterval(unlockTimer.current);
    setUnlockProgress(0);
  };

  // ========== 退出：确认后放弃会话 ==========
  const handleExit = async () => {
    if (!window.confirm('确定要放弃本次烹饪吗？进度将不会保留')) return;
    try {
      await abandonSession(sessionId);
    } catch {
      /* 放弃失败也允许离开 */
    }
    navigate('/', { replace: true });
  };

  // ========== 派生数据 ==========
  const step = session?.currentStep;
  const currentPhaseIdx = PHASES.findIndex((p) => p.key === session?.currentPhase);
  /** 当前阶段内的步骤序号 / 总数（如「煮 · 2/3」） */
  const phaseSteps = useMemo(
    () => (session?.steps || []).filter((s) => s.phase === session?.currentPhase),
    [session]
  );
  const stepPosInPhase = phaseSteps.findIndex((s) => s.id === step?.id) + 1;
  /** 是否全局最后一步（PLATE 末步，下一步即完成） */
  const isLastStep = useMemo(() => {
    const all = session?.steps || [];
    return all.length > 0 && step && all[all.length - 1].id === step.id;
  }, [session, step]);
  const fire = step?.firePower ? FIRE_POWER[step.firePower] : null;
  const runningTimers = timers.filter((t) => t.status === TIMER_RUNNING || t.remainSec > 0);
  /** 慢跑等效分钟：热量 / 11.6（与后端分享卡口径一致） */
  const jogMinutes = Math.round((recipeInfo?.caloriesKcal || 0) / 11.6);

  if (!session) return <PageLoading />;

  return (
    <div
      className="min-h-screen bg-paper flex flex-col select-none"
      onTouchStart={onTouchStart}
      onTouchEnd={onTouchEnd}
    >
      {/* ===== 顶栏：退出 / 菜名 / 锁定 ===== */}
      <header className="pt-safe">
        <div className="max-w-3xl mx-auto w-full px-4 pt-3 flex items-center gap-3">
          <button
            onClick={handleExit}
            aria-label="退出烹饪"
            className="w-9 h-9 rounded-full bg-ink/5 text-ink/60 flex items-center justify-center active:scale-95 transition shrink-0"
          >
            ✕
          </button>
          <p className="flex-1 text-center font-serif font-semibold truncate">{session.recipeName}</p>
          <button
            onClick={() => {
              setLocked(true);
              toast.success('已锁定屏幕，长按 2 秒可解锁');
            }}
            aria-label="锁定防误触"
            className="w-9 h-9 rounded-full bg-ink/5 text-ink/60 flex items-center justify-center active:scale-95 transition shrink-0"
          >
            🔓
          </button>
        </div>

        {/* ===== 整体进度条（progressPercent） ===== */}
        <div className="max-w-3xl mx-auto w-full px-4 mt-3">
          <div className="h-2 rounded-full bg-ink/10 overflow-hidden">
            <div
              className="h-full rounded-full bg-gradient-to-r from-warmth to-cinnabar transition-all duration-500"
              style={{ width: `${session.progressPercent || 0}%` }}
            />
          </div>
          <p className="text-right text-[10px] text-mute mt-1">{session.progressPercent || 0}%</p>
        </div>

        {/* ===== 五阶段印章指示器：备→洗→切→煮→盘 ===== */}
        <div className="max-w-3xl mx-auto w-full px-4 flex items-center justify-between">
          {PHASES.map((p, i) => {
            const state = i < currentPhaseIdx ? 'done' : i === currentPhaseIdx ? 'current' : 'todo';
            return (
              <div key={p.key} className="flex items-center flex-1 last:flex-none">
                <div className="flex flex-col items-center gap-1">
                  <span
                    className={`w-9 h-9 rounded-full font-serif text-sm flex items-center justify-center transition ${
                      state === 'current'
                        ? 'bg-cinnabar text-white shadow-seal animate-pulse-ring'
                        : state === 'done'
                          ? 'bg-jade/15 text-jade'
                          : 'bg-ink/5 text-mute'
                    }`}
                  >
                    {state === 'done' ? '✓' : p.name}
                  </span>
                  <span className={`text-[10px] ${state === 'current' ? 'text-cinnabar font-medium' : 'text-mute'}`}>
                    {p.weight}%
                  </span>
                </div>
                {/* 阶段连接线 */}
                {i < PHASES.length - 1 && (
                  <div className={`flex-1 h-px mx-1 mb-4 ${i < currentPhaseIdx ? 'bg-jade/40' : 'bg-ink/10'}`} />
                )}
              </div>
            );
          })}
        </div>
      </header>

      {/* ===== 中部：当前步骤大字展示（厨房远距离可读） ===== */}
      <main className="flex-1 flex flex-col justify-center max-w-3xl mx-auto w-full px-6 py-4">
        <div key={step?.id} className="animate-fade-up text-center space-y-5">
          <p className="text-sm text-mute tracking-widest">
            {PHASES[currentPhaseIdx]?.fullName || ''} · 第 {stepPosInPhase || 1}/{phaseSteps.length || 1} 步
          </p>
          {/* 步骤标题：特大字号 */}
          <h1 className="font-serif text-4xl md:text-6xl font-bold leading-tight">
            {step?.actionTitle || '准备开始'}
          </h1>
          {/* COOK 阶段火候徽标：大火🔥🔥🔥 / 中火🔥🔥 / 小火🔥 */}
          {session.currentPhase === 'COOK' && fire && (
            <div
              className={`inline-flex items-center gap-2 px-5 py-2 rounded-full text-white text-lg font-serif shadow-lift ${fire.bg}`}
            >
              <span className="animate-flicker">
                {fire.label === '大火' ? '🔥🔥🔥' : fire.label === '中火' ? '🔥🔥' : '🔥'}
              </span>
              {fire.label}
            </div>
          )}
          {/* 步骤详情：大字 */}
          <p className="text-xl md:text-2xl text-ink/75 leading-relaxed">{step?.detail}</p>
          {/* 含倒计时的步骤：一键开始计时 */}
          {step?.timerSec > 0 && (
            <button
              onClick={() => handleStartTimer(step)}
              className="btn-primary inline-flex items-center gap-2 px-8 py-3.5 text-lg font-serif"
            >
              ⏱ 开始计时 {formatSec(step.timerSec)}
            </button>
          )}
        </div>
      </main>

      {/* ===== 底部：计时器面板 + 翻页大按钮 + 麦克风 ===== */}
      <footer className="max-w-3xl mx-auto w-full px-4 pb-6 pb-safe space-y-3">
        {/* 多并行计时器面板 */}
        {runningTimers.length > 0 && (
          <div className="flex gap-2 overflow-x-auto no-scrollbar">
            {runningTimers.map((t) => {
              const done = t.remainSec <= 0;
              return (
                <div
                  key={t.id}
                  className={`card shrink-0 px-4 py-2.5 flex items-center gap-3 ${done ? 'bg-cinnabar/10' : ''}`}
                >
                  <div>
                    <p className="text-[10px] text-mute truncate max-w-[90px]">{t.timerName}</p>
                    <p
                      className={`font-mono font-bold text-xl leading-none ${
                        done ? 'text-cinnabar animate-flicker' : t.remainSec <= 10 ? 'text-cinnabar' : 'text-ink'
                      }`}
                    >
                      {done ? '完成' : formatSec(t.remainSec)}
                    </p>
                  </div>
                  <button
                    onClick={() => handleCancelTimer(t.id)}
                    aria-label="移除计时器"
                    className="text-mute text-xs w-6 h-6 rounded-full bg-ink/5 flex items-center justify-center active:scale-95"
                  >
                    ✕
                  </button>
                </div>
              );
            })}
          </div>
        )}

        {/* 翻页大按钮 + 语音麦克风（中间） */}
        <div className="flex items-center gap-3">
          <button
            onClick={goPrev}
            disabled={stepping}
            className="btn-ghost flex-1 py-4 text-lg font-serif disabled:opacity-50"
          >
            ← 上一步
          </button>
          <button
            onClick={handleMicClick}
            aria-label="语音指令"
            className={`w-14 h-14 rounded-full text-2xl flex items-center justify-center shrink-0 transition active:scale-95 ${
              listening ? 'bg-cinnabar text-white animate-pulse-ring' : 'bg-card shadow-soft'
            }`}
          >
            🎤
          </button>
          <button
            onClick={goNext}
            disabled={stepping}
            className="btn-primary flex-1 py-4 text-lg font-serif disabled:opacity-50"
          >
            {isLastStep ? '完成烹饪 🎉' : '下一步 →'}
          </button>
        </div>
        <p className="text-center text-[10px] text-mute">左右滑动翻页 · 也可以说「下一步」「计时」「还有多久」</p>
      </footer>

      {/* ===== 语音降级：文本指令输入弹层 ===== */}
      {voiceInputOpen && (
        <div
          className="fixed inset-0 z-50 bg-scrim/50 flex items-end md:items-center justify-center"
          onClick={() => setVoiceInputOpen(false)}
        >
          <div
            className="bg-card w-full md:max-w-sm rounded-t-3xl md:rounded-3xl p-6 pb-safe animate-slide-up md:animate-pop"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="font-serif text-lg font-bold">输入指令</h3>
            <p className="text-xs text-mute mt-1">当前设备不支持语音识别，改用文字试试：下一步 / 上一步 / 计时 / 还有多久</p>
            <form
              className="flex gap-2 mt-4"
              onSubmit={(e) => {
                e.preventDefault();
                handleVoiceText(voiceText);
                setVoiceText('');
                setVoiceInputOpen(false);
              }}
            >
              <input
                autoFocus
                className="input-base flex-1"
                placeholder="例如：下一步"
                value={voiceText}
                onChange={(e) => setVoiceText(e.target.value)}
              />
              <button type="submit" className="btn-primary px-5 text-sm shrink-0">
                发送
              </button>
            </form>
          </div>
        </div>
      )}

      {/* ===== 防误触锁定遮罩：长按 2 秒解锁 ===== */}
      {locked && (
        <div className="fixed inset-0 z-[60] bg-scrim/80 backdrop-blur-sm flex flex-col items-center justify-center gap-6 touch-none">
          <p className="text-white/80 font-serif text-lg tracking-widest">屏幕已锁定 · 防误触</p>
          <button
            onPointerDown={startUnlockPress}
            onPointerUp={cancelUnlockPress}
            onPointerLeave={cancelUnlockPress}
            onContextMenu={(e) => e.preventDefault()}
            className="relative w-24 h-24 rounded-full bg-white/10 border-2 border-white/40 text-white text-3xl flex items-center justify-center"
            aria-label="长按解锁"
          >
            🔒
            {/* 长按进度环 */}
            <svg className="absolute inset-0 -rotate-90" viewBox="0 0 96 96">
              <circle
                cx="48"
                cy="48"
                r="45"
                fill="none"
                stroke="#fff"
                strokeWidth="4"
                strokeLinecap="round"
                strokeDasharray={`${(unlockProgress / 100) * 283} 283`}
              />
            </svg>
          </button>
          <p className="text-white/60 text-xs">长按图标 2 秒解锁</p>
        </div>
      )}

      {/* ===== 完成成就卡片浮层 ===== */}
      {completed && (
        <div className="fixed inset-0 z-[70] bg-scrim/60 backdrop-blur-sm flex items-center justify-center px-6">
          <div className="bg-card w-full max-w-sm rounded-3xl p-7 text-center space-y-4 animate-pop">
            <span className="seal-badge w-16 h-16 text-3xl mx-auto animate-pop">🏆</span>
            <h2 className="font-serif text-2xl font-bold">出锅啦！</h2>
            <p className="font-serif text-lg text-cinnabar">{session.recipeName}</p>
            {/* 热量 + 慢跑等效换算 */}
            {recipeInfo?.caloriesKcal ? (
              <div className="bg-paper rounded-2xl py-3 px-4 text-sm text-ink/70 space-y-1">
                <p>
                  本餐约 <b className="text-warmth font-serif text-xl">{recipeInfo.caloriesKcal}</b> kcal
                </p>
                <p className="text-xs text-mute">相当于慢跑 {jogMinutes} 分钟，记得动一动～</p>
              </div>
            ) : (
              <p className="text-xs text-mute">辛苦啦，快趁热享用吧</p>
            )}
            {token && <p className="text-[11px] text-jade">✓ 已自动记入今日饮食日历</p>}
            <div className="space-y-2.5 pt-1">
              <button
                className="btn-primary w-full py-3 font-serif"
                onClick={() => navigate(`/community/post?recipeId=${session.recipeId}`)}
              >
                📸 拍照打卡晒装盘
              </button>
              <button className="btn-ghost w-full py-3" onClick={() => navigate('/')}>
                返回首页
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

import { useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { login, register } from '../api/user';
import useAuthStore from '../store/useAuthStore';
import { toast } from '../store/useToastStore';

/**
 * 登录 / 注册页：双卡切换；演示账号 demo/123456
 */
export default function Login() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const setAuth = useAuthStore((s) => s.setAuth);

  const [mode, setMode] = useState('login'); // login | register
  const [form, setForm] = useState({ username: '', password: '', nickname: '' });
  const [loading, setLoading] = useState(false);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  /** 表单校验 */
  const validate = () => {
    if (!form.username.trim()) return '请输入用户名';
    if (form.username.trim().length < 3) return '用户名至少 3 个字符';
    if (!form.password) return '请输入密码';
    if (form.password.length < 6) return '密码至少 6 位';
    if (mode === 'register' && !form.nickname.trim()) return '请输入昵称';
    return '';
  };

  const submit = async (e) => {
    e.preventDefault();
    const msg = validate();
    if (msg) return toast.error(msg);
    setLoading(true);
    try {
      const data =
        mode === 'login'
          ? await login({ username: form.username.trim(), password: form.password })
          : await register({
              username: form.username.trim(),
              password: form.password,
              nickname: form.nickname.trim(),
            });
      setAuth(data.token, data.user);
      toast.success(mode === 'login' ? `欢迎回来，${data.user?.nickname || ''}` : '注册成功，欢迎加入食研社');
      navigate(decodeURIComponent(params.get('redirect') || '/'), { replace: true });
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-paper flex flex-col items-center justify-center px-6 py-10">
      {/* 品牌区 */}
      <Link to="/" className="flex flex-col items-center gap-3 mb-8">
        <span className="seal-badge w-16 h-16 text-3xl animate-pop">食</span>
        <h1 className="font-serif text-2xl font-bold tracking-[0.3em]">食研社</h1>
        <p className="text-xs text-mute tracking-widest">精研一餐一食 · 新中式烹饪助手</p>
      </Link>

      <div className="card w-full max-w-sm p-6 animate-fade-up">
        {/* 双卡切换 */}
        <div className="flex bg-paper rounded-xl p-1 mb-6">
          {[
            { key: 'login', label: '登 录' },
            { key: 'register', label: '注 册' },
          ].map((t) => (
            <button
              key={t.key}
              type="button"
              onClick={() => setMode(t.key)}
              className={`flex-1 py-2 rounded-lg text-sm font-serif transition ${
                mode === t.key ? 'bg-cinnabar text-white shadow-seal' : 'text-mute'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        <form onSubmit={submit} className="space-y-4">
          <div>
            <label className="block text-xs text-mute mb-1.5">用户名</label>
            <input
              className="input-base"
              placeholder="3 个字符以上"
              value={form.username}
              onChange={set('username')}
              autoComplete="username"
            />
          </div>
          {mode === 'register' && (
            <div className="animate-fade-up">
              <label className="block text-xs text-mute mb-1.5">昵称</label>
              <input
                className="input-base"
                placeholder="给自己起个好听的名字"
                value={form.nickname}
                onChange={set('nickname')}
              />
            </div>
          )}
          <div>
            <label className="block text-xs text-mute mb-1.5">密码</label>
            <input
              type="password"
              className="input-base"
              placeholder="至少 6 位"
              value={form.password}
              onChange={set('password')}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            />
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full py-3 text-base font-serif">
            {loading ? '请稍候…' : mode === 'login' ? '进入食研社' : '注册并登录'}
          </button>
        </form>

        {/* 演示账号提示 */}
        <div className="mt-5 text-center text-xs text-mute bg-jade/5 text-jade/80 rounded-xl py-2.5">
          演示账号：<b>demo</b> / <b>123456</b>
          <button
            type="button"
            className="ml-2 underline underline-offset-2"
            onClick={() => {
              setMode('login');
              setForm((f) => ({ ...f, username: 'demo', password: '123456' }));
            }}
          >
            一键填入
          </button>
        </div>
      </div>

      <Link to="/" className="mt-6 text-xs text-mute underline underline-offset-4">
        先逛逛，暂不登录 →
      </Link>
    </div>
  );
}

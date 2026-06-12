import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import useAuthStore from '../store/useAuthStore';
import ThemeToggle from './ThemeToggle';

/** 四个主 Tab 配置 */
const TABS = [
  { to: '/', label: '首页', icon: '🏠' },
  { to: '/search', label: '搜索', icon: '🔍' },
  { to: '/community', label: '社区', icon: '🍽️' },
  { to: '/profile', label: '我的', icon: '👤' },
];

/**
 * 公共布局：
 * - 移动端：底部 TabBar（中央凸起烹饪图标装饰）+ safe-area 适配
 * - PC（md+）：顶部水平导航，内容 max-w-5xl 居中
 */
export default function Layout() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  return (
    <div className="min-h-screen bg-paper">
      {/* ===== PC 顶部导航 ===== */}
      <header className="hidden md:block sticky top-0 z-40 bg-paper/90 backdrop-blur border-b border-ink/5">
        <div className="max-w-5xl mx-auto px-4 h-14 flex items-center justify-between">
          <NavLink to="/" className="flex items-center gap-2">
            <span className="seal-badge w-8 h-8 text-lg">食</span>
            <span className="font-serif font-bold text-lg tracking-wide">食研社</span>
          </NavLink>
          <nav className="flex items-center gap-1">
            {TABS.map((t) => (
              <NavLink
                key={t.to}
                to={t.to}
                end={t.to === '/'}
                className={({ isActive }) =>
                  `px-4 py-1.5 rounded-xl text-sm transition ${
                    isActive ? 'bg-cinnabar text-white shadow-seal' : 'text-ink/70 hover:bg-ink/5'
                  }`
                }
              >
                {t.label}
              </NavLink>
            ))}
          </nav>
          <div className="flex items-center gap-2">
            {/* 主题切换：太阳/月亮/跟随系统 */}
            <ThemeToggle />
            <button
              onClick={() => navigate(user ? '/profile' : '/login')}
              className="text-sm text-mute hover:text-cinnabar transition"
            >
              {user ? `嗨，${user.nickname || user.username}` : '登录 / 注册'}
            </button>
          </div>
        </div>
      </header>

      {/* ===== 页面内容 ===== */}
      <main className="pb-24 md:pb-10">
        <Outlet />
      </main>

      {/* ===== 移动端底部 TabBar ===== */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-card/95 backdrop-blur border-t border-ink/5 pb-safe">
        <div className="relative grid grid-cols-5 h-14">
          {TABS.slice(0, 2).map((t) => <TabItem key={t.to} tab={t} />)}
          {/* 中央凸起烹饪图标（装饰，点击回首页推荐） */}
          <div className="relative flex justify-center">
            <button
              onClick={() => navigate('/')}
              aria-label="食研社"
              className="absolute -top-5 w-12 h-12 rounded-full bg-cinnabar text-white text-xl shadow-seal flex items-center justify-center active:scale-95 transition border-4 border-paper"
            >
              🍳
            </button>
          </div>
          {TABS.slice(2).map((t) => <TabItem key={t.to} tab={t} />)}
        </div>
      </nav>
    </div>
  );
}

/** 单个 Tab 项 */
function TabItem({ tab }) {
  return (
    <NavLink
      to={tab.to}
      end={tab.to === '/'}
      className={({ isActive }) =>
        `flex flex-col items-center justify-center gap-0.5 text-[10px] transition ${
          isActive ? 'text-cinnabar font-medium' : 'text-mute'
        }`
      }
    >
      <span className="text-lg leading-none">{tab.icon}</span>
      {tab.label}
    </NavLink>
  );
}

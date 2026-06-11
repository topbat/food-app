import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import ToastContainer from './components/Toast';
import { PageLoading } from './components/Loading';

// ===== 全部页面懒加载，按路由分包 =====
const Login = lazy(() => import('./pages/Login'));
const Home = lazy(() => import('./pages/Home'));
const Search = lazy(() => import('./pages/Search'));
const RecipeDetail = lazy(() => import('./pages/RecipeDetail'));
const Cooking = lazy(() => import('./pages/Cooking'));
const Community = lazy(() => import('./pages/Community'));
const PostCreate = lazy(() => import('./pages/PostCreate'));
const Profile = lazy(() => import('./pages/Profile'));
const UgcUpload = lazy(() => import('./pages/UgcUpload'));

/**
 * 路由结构：
 * - Layout 内：首页/搜索/详情/社区/发帖/我的/UGC（带 TabBar 或顶部导航）
 * - Layout 外：登录页、厨房模式（全屏沉浸）
 */
export default function App() {
  return (
    <>
      <Suspense fallback={<PageLoading />}>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Home />} />
            <Route path="/search" element={<Search />} />
            <Route path="/recipe/:id" element={<RecipeDetail />} />
            <Route path="/community" element={<Community />} />
            <Route path="/community/post" element={<PostCreate />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/profile/ugc" element={<UgcUpload />} />
            <Route path="*" element={<Home />} />
          </Route>
          <Route path="/login" element={<Login />} />
          <Route path="/cooking/:sessionId" element={<Cooking />} />
        </Routes>
      </Suspense>
      <ToastContainer />
    </>
  );
}

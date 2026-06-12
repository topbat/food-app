import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import './index.css';
// 引入即初始化主题（挂 .dark 类 + 监听系统主题变化），保证 Layout 外的页面（如厨房模式）也生效
import './store/useThemeStore';

// 应用入口：挂载根组件
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);

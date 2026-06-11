import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * Vite 配置：开发服务器 5173，/api/* 按模块代理到各后端微服务
 */
const services = {
  user: 8081, // 用户与画像服务
  recipe: 8082, // 菜谱结构化服务
  kitchen: 8083, // 厨房引擎服务
  search: 8084, // 智能搜索推荐服务
  social: 8085, // 社交与UGC服务
  ai: 8086, // AI 服务
};

const proxy = {};
for (const [name, port] of Object.entries(services)) {
  proxy[`/api/${name}`] = {
    target: `http://localhost:${port}`,
    changeOrigin: true,
  };
}

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
    proxy,
  },
  build: {
    chunkSizeWarningLimit: 800,
  },
});

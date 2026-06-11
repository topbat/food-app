import client from './client';

// ===== 搜索服务 search-service :8084 =====

/**
 * 搜索菜谱
 * @param params {keyword?,ingredients?(逗号分隔),maxCalories?,tagName?,searchType(1关键词2食材3营养4人群),page,size}
 */
export const searchRecipes = (params) => client.get('/api/search/recipes', { params });

/** 热搜 TOP10 */
export const getHotKeywords = () => client.get('/api/search/hot');

/** 我的搜索历史（最近20条，需登录） */
export const getSearchHistory = () => client.get('/api/search/history');

/** 清空搜索历史 */
export const clearSearchHistory = () => client.delete('/api/search/history');

/** 个性化推荐（登录按标签，游客按热度）→ {reason,list} */
export const getRecommend = (limit = 10) =>
  client.get('/api/search/recommend', { params: { limit } });

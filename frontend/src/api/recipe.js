import client from './client';

// ===== 菜谱服务 recipe-service :8082 =====

/** 菜谱分页查询（支持菜系/难度/关键词/热量上限/标签/食材筛选） */
export const getRecipeList = (params) => client.get('/api/recipe/list', { params });

/** 菜谱详情（info + stepsByPhase 五阶段 + ingredients + 适宜/慎用标签 + 阶段权重） */
export const getRecipeDetail = (id) => client.get(`/api/recipe/${id}`);

/** 菜谱步骤平铺列表 */
export const getRecipeSteps = (id) => client.get(`/api/recipe/${id}/steps`);

/** 智能替换：scene ∈ 减脂/控糖/素食/低嘌呤 → 替换明细与热量差 */
export const substituteRecipe = (id, scene) =>
  client.post(`/api/recipe/${id}/substitute`, { scene });

/** 浏览数 +1（进详情页时调用，失败静默） */
export const viewRecipe = (id) => client.post(`/api/recipe/${id}/view`);

/** UGC 上传菜谱（五步法模板，须包含全部 5 阶段步骤） */
export const uploadUgcRecipe = (data) => client.post('/api/recipe/ugc', data);

/** 食材库（含分类与每100g营养数据） */
export const getIngredientList = () => client.get('/api/recipe/ingredient/list');

/** 标签字典（tagType 区分人群/场景等） */
export const getTagList = () => client.get('/api/recipe/tag/list');

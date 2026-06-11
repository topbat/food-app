import client from './client';

// ===== 用户服务 user-service :8081 =====

/** 注册：{username,password,nickname} → {token,user} */
export const register = (data) => client.post('/api/user/register', data);

/** 登录：{username,password} → {token,user} */
export const login = (data) => client.post('/api/user/login', data);

/** 获取用户公开信息（昵称头像） */
export const getPublicUser = (id) => client.get(`/api/user/public/${id}`);

/** 获取个人主页（user + profile 健康档案 + tags） */
export const getProfile = () => client.get('/api/user/profile');

/** 更新健康档案（身高体重过敏史偏好/健康目标/每日热量目标等） */
export const updateProfile = (data) => client.put('/api/user/profile', data);

/** 我的标签列表 */
export const getMyTags = () => client.get('/api/user/tag/list');

/** 添加个人标签：{tagName,tagType} */
export const addTag = (data) => client.post('/api/user/tag', data);

/** 删除个人标签 */
export const deleteTag = (id) => client.delete(`/api/user/tag/${id}`);

/** 记录一条饮食：{recordDate,mealType,recipeId?,recipeName,caloriesKcal,...} */
export const addDietRecord = (data) => client.post('/api/user/diet', data);

/** 当日饮食统计（记录列表+总热量+目标对比） */
export const getDietDaily = (date) => client.get('/api/user/diet/daily', { params: { date } });

/** 月度饮食日历（每日总热量） */
export const getDietMonth = (month) => client.get('/api/user/diet/month', { params: { month } });

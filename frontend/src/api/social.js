import client from './client';

// ===== 社交服务 social-service :8085 =====

/** 发评论：{targetType,targetId,stepId?,content,parentId?} */
export const addComment = (data) => client.post('/api/social/comment', data);

/** 评论列表（targetType=1菜谱）分页 */
export const getCommentList = (params) => client.get('/api/social/comment/list', { params });

/** 发帖（晒作品/日记）：{recipeId?,content,imageUrls,postType} */
export const createPost = (data) => client.post('/api/social/post', data);

/** 作品墙列表（分页，可按 postType 筛选） */
export const getPostList = (params) => client.get('/api/social/post/list', { params });

/** 点赞 toggle：{targetType,targetId} → {liked,likeCount} */
export const toggleLike = (data) => client.post('/api/social/like', data);

/** 评分（1-5 分，重复评分则更新）：{recipeId,score} */
export const rateRecipe = (data) => client.post('/api/social/rating', data);

/** 获取菜谱评分概要 {avgScore,ratingCount} */
export const getRating = (recipeId) => client.get(`/api/social/rating/${recipeId}`);

/** 打卡：{recipeId?,note?} → {continuousDays,newBadges} */
export const checkin = (data) => client.post('/api/social/checkin', data);

/** 我的打卡日历：{dates:[],continuousDays} */
export const getMyCheckin = (month) => client.get('/api/social/checkin/my', { params: { month } });

/** 我的徽章墙（全部徽章 + 是否已获得） */
export const getMyBadges = () => client.get('/api/social/badge/my');

/** 分享卡数据 */
export const getShareCard = (data) => client.post('/api/social/share-card', data);

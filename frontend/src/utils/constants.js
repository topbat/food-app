// ===== 全局常量字典 =====

/** 菜系映射：cuisineType 1-9 */
export const CUISINES = [
  { type: 1, name: '川菜', emoji: '🌶️' },
  { type: 2, name: '鲁菜', emoji: '🥟' },
  { type: 3, name: '粤菜', emoji: '🦐' },
  { type: 4, name: '苏菜', emoji: '🦆' },
  { type: 5, name: '闽菜', emoji: '🐟' },
  { type: 6, name: '浙菜', emoji: '🍤' },
  { type: 7, name: '湘菜', emoji: '🔥' },
  { type: 8, name: '徽菜', emoji: '🍲' },
  { type: 9, name: '家常菜', emoji: '🥘' },
];

/** 难度映射 */
export const DIFFICULTY = { 1: '简单', 2: '进阶', 3: '大师' };

/** 五阶段：键名、中文、权重、印章色 */
export const PHASES = [
  { key: 'PREPARE', name: '备', fullName: '前期准备', weight: 10 },
  { key: 'WASH', name: '洗', fullName: '清洗', weight: 5 },
  { key: 'CUT', name: '切', fullName: '切配', weight: 15 },
  { key: 'COOK', name: '煮', fullName: '烹煮', weight: 60 },
  { key: 'PLATE', name: '盘', fullName: '装盘', weight: 10 },
];

/** 火候映射（COOK 阶段火候指示灯） */
export const FIRE_POWER = {
  HIGH: { label: '大火', emoji: '🔥', color: 'text-cinnabar', bg: 'bg-cinnabar' },
  MID: { label: '中火', emoji: '🔥', color: 'text-warmth', bg: 'bg-warmth' },
  LOW: { label: '小火', emoji: '🕯️', color: 'text-ochre', bg: 'bg-ochre' },
  大火: { label: '大火', emoji: '🔥', color: 'text-cinnabar', bg: 'bg-cinnabar' },
  中火: { label: '中火', emoji: '🔥', color: 'text-warmth', bg: 'bg-warmth' },
  小火: { label: '小火', emoji: '🕯️', color: 'text-ochre', bg: 'bg-ochre' },
};

/** 餐次映射 */
export const MEAL_TYPES = { 1: '早餐', 2: '午餐', 3: '晚餐', 4: '加餐' };

/** 按当前时间推断餐次：早10前=早餐，15前=午餐，21前=晚餐，否则加餐 */
export function inferMealType() {
  const h = new Date().getHours();
  if (h < 10) return 1;
  if (h < 15) return 2;
  if (h < 21) return 3;
  return 4;
}

/** 智能替换场景 */
export const SUBSTITUTE_SCENES = ['减脂', '控糖', '素食', '低嘌呤'];

/** 帖子类型 */
export const POST_TYPES = [
  { value: 1, label: '晒作品' },
  { value: 2, label: '美食日记' },
  { value: 3, label: '经验分享' },
];

/** 健康目标下拉项 */
export const HEALTH_GOALS = ['减脂', '增肌', '控糖', '均衡饮食', '低盐低脂'];

/** 占位图（封面加载失败时使用，内联 SVG） */
export const PLACEHOLDER_IMG =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 400 300'%3E%3Crect width='400' height='300' fill='%23F0EADF'/%3E%3Ctext x='200' y='150' font-size='64' text-anchor='middle' dominant-baseline='middle'%3E%F0%9F%8D%B2%3C/text%3E%3C/svg%3E";

import { create } from 'zustand';

/**
 * 烹饪会话状态：当前会话 VO、本地计时器快照
 * （会话本体以后端为准，这里做页面间共享与本地倒计时缓冲）
 */
const useCookingStore = create((set) => ({
  /** 当前会话 SessionVO */
  session: null,
  /** 正在烹饪的菜谱概要（用于完成卡展示热量等） */
  recipeInfo: null,
  setSession: (session) => set({ session }),
  setRecipeInfo: (recipeInfo) => set({ recipeInfo }),
  /** 离开厨房模式时清空 */
  clear: () => set({ session: null, recipeInfo: null }),
}));

export default useCookingStore;

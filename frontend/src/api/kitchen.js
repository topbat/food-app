import client from './client';

// ===== 厨房服务 kitchen-service :8083（全部需鉴权） =====

/** 开始烹饪：创建会话 {recipeId} → SessionVO */
export const createSession = (recipeId) => client.post('/api/kitchen/session', { recipeId });

/** 查询会话详情（含 steps/currentStep/progressPercent/timers） */
export const getSession = (id) => client.get(`/api/kitchen/session/${id}`);

/** 下一步（阶段末自动进入下一阶段，PLATE 最后一步自动完成） */
export const nextStep = (id) => client.post(`/api/kitchen/session/${id}/next`);

/** 上一步（仅允许阶段内回退，跨阶段返回 40900） */
export const prevStep = (id) => client.post(`/api/kitchen/session/${id}/prev`);

/** 主动完成烹饪 */
export const finishSession = (id) => client.post(`/api/kitchen/session/${id}/finish`);

/** 放弃烹饪 */
export const abandonSession = (id) => client.post(`/api/kitchen/session/${id}/abandon`);

/** 开启一个计时器：{timerName,totalSec}（支持多个并行） */
export const startTimer = (id, data) => client.post(`/api/kitchen/session/${id}/timer`, data);

/** 计时器列表（后端到点自动置为已完成） */
export const getTimers = (id) => client.get(`/api/kitchen/session/${id}/timer`);

/** 取消计时器 */
export const cancelTimer = (timerId) => client.post(`/api/kitchen/timer/${timerId}/cancel`);

/** 语音指令：{commandText} → {parsedAction,message,session?} */
export const sendVoiceCommand = (id, commandText) =>
  client.post(`/api/kitchen/session/${id}/voice`, { commandText });

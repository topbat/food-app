package com.foodapp.kitchen.service;

import com.foodapp.kitchen.dto.SessionVO;
import com.foodapp.kitchen.dto.StepDTO;
import com.foodapp.kitchen.dto.VoiceResultVO;
import com.foodapp.kitchen.entity.KitchenTimer;
import com.foodapp.kitchen.entity.KitchenVoiceLog;
import com.foodapp.kitchen.repository.KitchenVoiceLogRepository;
import com.foodapp.kitchen.support.SessionGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringJoiner;

/**
 * 语音指令解析服务。
 * 解析规则（按优先级匹配，与接口契约第3节一致）：
 * 1. 含"下一步"或"继续" → NEXT_STEP（推进状态机）；
 * 2. 含"上一步"或"回退" → PREV_STEP（仅阶段内回退，跨阶段回退抛 40900）；
 * 3. 含"还有多久"或"几分钟" → QUERY_TIMER（返回进行中计时器剩余时间文案，如"焖煮还剩2分30秒"）；
 * 4. 含"计时" → START_TIMER（当前步骤 timerSec&gt;0 时以该值开计时）；
 * 5. 其余 → UNKNOWN（message="未能识别指令"）。
 * 每条指令均落库 kitchen_voice_log（与后续动作是否成功无关），便于离线分析语音识别效果。
 */
@Service
public class KitchenVoiceService {

    private static final Logger log = LoggerFactory.getLogger(KitchenVoiceService.class);

    /** 解析动作：下一步 */
    public static final String ACTION_NEXT_STEP = "NEXT_STEP";
    /** 解析动作：上一步 */
    public static final String ACTION_PREV_STEP = "PREV_STEP";
    /** 解析动作：查询计时器剩余时间 */
    public static final String ACTION_QUERY_TIMER = "QUERY_TIMER";
    /** 解析动作：开启计时器 */
    public static final String ACTION_START_TIMER = "START_TIMER";
    /** 解析动作：未识别 */
    public static final String ACTION_UNKNOWN = "UNKNOWN";

    private final KitchenSessionService sessionService;
    private final KitchenTimerService timerService;
    private final KitchenVoiceLogRepository voiceLogRepository;
    private final SessionGuard sessionGuard;

    public KitchenVoiceService(KitchenSessionService sessionService,
                               KitchenTimerService timerService,
                               KitchenVoiceLogRepository voiceLogRepository,
                               SessionGuard sessionGuard) {
        this.sessionService = sessionService;
        this.timerService = timerService;
        this.voiceLogRepository = voiceLogRepository;
        this.sessionGuard = sessionGuard;
    }

    /**
     * 处理一条语音指令：归属校验 → 解析动作 → 落库日志 → 执行动作 → 组装结果。
     * 说明：本方法不加事务，保证语音日志先行独立提交；
     * 后续动作（如跨阶段回退 40900）失败不会回滚已落库的指令日志。
     *
     * @param sessionId   会话ID（先做归属校验，防越权）
     * @param commandText 用户语音指令原文
     * @return 解析结果（parsedAction + 中文反馈文案 + 最新会话快照，UNKNOWN/查询类不返回会话）
     */
    public VoiceResultVO handleCommand(Long sessionId, String commandText) {
        // 归属校验前置：操作他人会话（40300）/会话不存在（40400）时不落库、不执行
        sessionGuard.loadOwnedSession(sessionId);

        String action = parseAction(commandText);
        saveVoiceLog(sessionId, commandText, action);
        log.info("语音指令: '{}' -> {}", commandText, action);

        VoiceResultVO result = new VoiceResultVO();
        result.setParsedAction(action);
        switch (action) {
            case ACTION_NEXT_STEP -> executeNext(sessionId, result);
            case ACTION_PREV_STEP -> executePrev(sessionId, result);
            case ACTION_QUERY_TIMER -> executeQueryTimer(sessionId, result);
            case ACTION_START_TIMER -> executeStartTimer(sessionId, result);
            default -> result.setMessage("未能识别指令");
        }
        return result;
    }

    /**
     * 按契约规则解析指令文本对应的动作（按规则顺序优先匹配）。
     *
     * @param commandText 指令原文
     * @return 动作编码（NEXT_STEP/PREV_STEP/QUERY_TIMER/START_TIMER/UNKNOWN）
     */
    public String parseAction(String commandText) {
        String text = commandText == null ? "" : commandText;
        if (text.contains("下一步") || text.contains("继续")) {
            return ACTION_NEXT_STEP;
        }
        if (text.contains("上一步") || text.contains("回退")) {
            return ACTION_PREV_STEP;
        }
        if (text.contains("还有多久") || text.contains("几分钟")) {
            return ACTION_QUERY_TIMER;
        }
        if (text.contains("计时")) {
            return ACTION_START_TIMER;
        }
        return ACTION_UNKNOWN;
    }

    /**
     * 执行"下一步"：推进状态机并组装反馈文案。
     *
     * @param sessionId 会话ID
     * @param result    待填充的结果对象
     */
    private void executeNext(Long sessionId, VoiceResultVO result) {
        SessionVO session = sessionService.nextStep(sessionId);
        result.setSession(session);
        // 关键判断：PLATE 最后一步再 next → 状态机自动完成
        if (session.getStatus() != null && session.getStatus() == 2) {
            result.setMessage("全部步骤已完成，烹饪结束，开吃吧！");
        } else {
            result.setMessage("已进入下一步" + describeStep(session.getCurrentStep()));
        }
    }

    /**
     * 执行"上一步"：阶段内回退；跨阶段回退由状态机抛出 40900（烹饪阶段不可逆向跳跃）。
     *
     * @param sessionId 会话ID
     * @param result    待填充的结果对象
     */
    private void executePrev(Long sessionId, VoiceResultVO result) {
        SessionVO session = sessionService.prevStep(sessionId);
        result.setSession(session);
        result.setMessage("已回到上一步" + describeStep(session.getCurrentStep()));
    }

    /**
     * 执行"还有多久"：汇总会话下全部进行中计时器的剩余时间文案。
     *
     * @param sessionId 会话ID
     * @param result    待填充的结果对象
     */
    private void executeQueryTimer(Long sessionId, VoiceResultVO result) {
        List<KitchenTimer> running = timerService.listRunningTimers(sessionId);
        // 关键判断：无进行中的计时器时给出明确反馈而非空文案
        if (running.isEmpty()) {
            result.setMessage("当前没有进行中的计时器");
            return;
        }
        StringJoiner joiner = new StringJoiner("，");
        for (KitchenTimer timer : running) {
            joiner.add(timer.getTimerName() + "还剩" + formatDuration(timerService.computeRemainSec(timer)));
        }
        result.setMessage(joiner.toString());
    }

    /**
     * 执行"计时"：当前步骤 timerSec&gt;0 时以该值开计时（计时器名取步骤动作标题）；
     * 否则提示当前步骤无需计时。
     *
     * @param sessionId 会话ID
     * @param result    待填充的结果对象
     */
    private void executeStartTimer(Long sessionId, VoiceResultVO result) {
        SessionVO session = sessionService.getSession(sessionId);
        StepDTO currentStep = session.getCurrentStep();
        // 关键判断：当前步骤未配置计时秒数（timerSec<=0）则不开计时器
        if (currentStep == null || currentStep.getTimerSec() == null || currentStep.getTimerSec() <= 0) {
            log.info("[语音] 会话{}当前步骤无计时配置，不开启计时器", sessionId);
            result.setMessage("当前步骤无需计时");
            result.setSession(session);
            return;
        }
        timerService.createTimer(sessionId, currentStep.getActionTitle(), currentStep.getTimerSec());
        result.setMessage("已为「" + currentStep.getActionTitle() + "」开启"
                + formatDuration(currentStep.getTimerSec()) + "计时");
        // 重新取会话快照，使 timers 列表包含刚开启的计时器
        result.setSession(sessionService.getSession(sessionId));
    }

    /**
     * 落库语音指令日志（kitchen_voice_log），用于离线分析语音识别命中率。
     *
     * @param sessionId   会话ID
     * @param commandText 指令原文
     * @param action      解析出的动作
     */
    private void saveVoiceLog(Long sessionId, String commandText, String action) {
        KitchenVoiceLog voiceLog = new KitchenVoiceLog();
        voiceLog.setSessionId(sessionId);
        voiceLog.setCommandText(commandText);
        voiceLog.setParsedAction(action);
        voiceLogRepository.save(voiceLog);
    }

    /**
     * 步骤的中文描述（用于语音反馈文案）。
     *
     * @param step 步骤（可能为 null）
     * @return 形如"：切丁"的后缀；步骤为空返回空串
     */
    private String describeStep(StepDTO step) {
        if (step == null || step.getActionTitle() == null || step.getActionTitle().isBlank()) {
            return "";
        }
        return "：" + step.getActionTitle();
    }

    /**
     * 秒数格式化为中文时长文案（如 150 → "2分30秒"，120 → "2分钟"，45 → "45秒"）。
     *
     * @param totalSec 总秒数
     * @return 中文时长文案
     */
    private String formatDuration(long totalSec) {
        long minutes = totalSec / 60;
        long seconds = totalSec % 60;
        if (minutes > 0 && seconds > 0) {
            return minutes + "分" + seconds + "秒";
        }
        if (minutes > 0) {
            return minutes + "分钟";
        }
        return seconds + "秒";
    }
}

package com.foodapp.kitchen.service;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.kitchen.dto.SessionVO;
import com.foodapp.kitchen.dto.StepDTO;
import com.foodapp.kitchen.entity.KitchenSession;
import com.foodapp.kitchen.remote.RecipeClient;
import com.foodapp.kitchen.repository.KitchenSessionRepository;
import com.foodapp.kitchen.support.Phases;
import com.foodapp.kitchen.support.SessionGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 烹饪会话服务（五步法状态机核心）。
 * 状态机规则：
 * 1. 阶段顺序 PREPARE→WASH→CUT→COOK→PLATE，只进不退（跨阶段）；
 * 2. next：阶段内有下一步则 step_index+1，否则进入下一个有步骤的阶段第1步；
 *    PLATE 最后一步再 next → status=2 已完成、记录 finished_at；
 * 3. prev：仅允许阶段内回退（step_index>1 时-1）；已在阶段第1步仍要回退 → 抛 40900
 *    「烹饪阶段不可逆向跳跃」（产品安全设计：防止生熟流程倒置引发食品安全问题）。
 */
@Service
public class KitchenSessionService {

    private static final Logger log = LoggerFactory.getLogger(KitchenSessionService.class);

    private final KitchenSessionRepository sessionRepository;
    private final KitchenTimerService timerService;
    private final RecipeClient recipeClient;
    private final SessionGuard sessionGuard;

    public KitchenSessionService(KitchenSessionRepository sessionRepository,
                                 KitchenTimerService timerService,
                                 RecipeClient recipeClient,
                                 SessionGuard sessionGuard) {
        this.sessionRepository = sessionRepository;
        this.timerService = timerService;
        this.recipeClient = recipeClient;
        this.sessionGuard = sessionGuard;
    }

    /**
     * 开始烹饪：调菜谱服务取菜谱名与步骤，创建会话（初始 PREPARE 阶段第1步）。
     *
     * @param recipeId 菜谱ID
     * @return 会话视图对象
     */
    @Transactional
    public SessionVO createSession(Long recipeId) {
        Long userId = UserContext.requireUserId();
        String recipeName = recipeClient.fetchRecipeName(recipeId);
        List<StepDTO> steps = recipeClient.fetchSteps(recipeId);
        // 关键判断：步骤为空的菜谱无法进入厨房模式
        if (steps.isEmpty()) {
            log.warn("[会话] 菜谱{}步骤为空，无法开始烹饪", recipeId);
            throw new BusinessException(ResultCode.BIZ_CONFLICT, "该菜谱暂无步骤，无法开始烹饪");
        }
        KitchenSession session = new KitchenSession();
        session.setUserId(userId);
        session.setRecipeId(recipeId);
        session.setRecipeName(recipeName);
        session.setCurrentPhase(firstPhaseWithSteps(steps));
        session.setCurrentStepIndex(1);
        session.setStatus(KitchenSession.STATUS_RUNNING);
        session.setStartedAt(LocalDateTime.now());
        sessionRepository.save(session);
        log.info("[会话] 用户{}开始烹饪《{}》(菜谱{}), 会话{}, 初始阶段{}.1",
                userId, recipeName, recipeId, session.getId(), session.getCurrentPhase());
        return toVO(session, steps);
    }

    /**
     * 查询会话详情（含全量步骤、当前步骤、计时器、进度百分比）。
     *
     * @param sessionId 会话ID
     * @return 会话视图对象
     */
    @Transactional
    public SessionVO getSession(Long sessionId) {
        KitchenSession session = sessionGuard.loadOwnedSession(sessionId);
        return toVO(session, recipeClient.fetchSteps(session.getRecipeId()));
    }

    /**
     * 下一步：阶段内推进，阶段末自动进入下一阶段；PLATE 最后一步再 next → 烹饪完成。
     *
     * @param sessionId 会话ID
     * @return 流转后的会话视图对象
     */
    @Transactional
    public SessionVO nextStep(Long sessionId) {
        KitchenSession session = loadRunningSession(sessionId);
        List<StepDTO> steps = recipeClient.fetchSteps(session.getRecipeId());
        Map<String, List<StepDTO>> byPhase = groupByPhase(steps);

        String fromPhase = session.getCurrentPhase();
        int fromIndex = session.getCurrentStepIndex();
        int phaseStepCount = byPhase.getOrDefault(fromPhase, List.of()).size();

        if (fromIndex < phaseStepCount) {
            // 关键判断：当前阶段内还有下一步 → 阶段内推进
            session.setCurrentStepIndex(fromIndex + 1);
        } else {
            String nextPhase = nextPhaseWithSteps(byPhase, fromPhase);
            if (nextPhase == null) {
                // 关键判断：已是 PLATE（最后一个有步骤的阶段）最后一步 → 烹饪完成
                session.setStatus(KitchenSession.STATUS_FINISHED);
                session.setFinishedAt(LocalDateTime.now());
                sessionRepository.save(session);
                log.info("[状态机] 会话{}烹饪完成：《{}》全部步骤已走完", sessionId, session.getRecipeName());
                return toVO(session, steps);
            }
            // 阶段末 → 进入下一阶段第1步
            session.setCurrentPhase(nextPhase);
            session.setCurrentStepIndex(1);
        }
        sessionRepository.save(session);
        log.info("[状态机] 会话{}流转: {}.{} -> {}.{}",
                sessionId, fromPhase, fromIndex, session.getCurrentPhase(), session.getCurrentStepIndex());
        return toVO(session, steps);
    }

    /**
     * 上一步：仅允许阶段内回退；已在阶段第1步仍要回退 → 抛 40900「烹饪阶段不可逆向跳跃」。
     *
     * @param sessionId 会话ID
     * @return 流转后的会话视图对象
     */
    @Transactional
    public SessionVO prevStep(Long sessionId) {
        KitchenSession session = loadRunningSession(sessionId);
        String phase = session.getCurrentPhase();
        int index = session.getCurrentStepIndex();
        // 关键判断：已在阶段第1步还要回退 → 跨阶段逆向跳跃，产品安全设计禁止（防生熟流程倒置）
        if (index <= 1) {
            log.warn("[状态机] 会话{}在阶段{}第1步尝试跨阶段回退，已拦截（烹饪阶段不可逆向跳跃）", sessionId, phase);
            throw new BusinessException(ResultCode.BIZ_CONFLICT, "烹饪阶段不可逆向跳跃");
        }
        session.setCurrentStepIndex(index - 1);
        sessionRepository.save(session);
        log.info("[状态机] 会话{}流转: {}.{} -> {}.{}",
                sessionId, phase, index, phase, session.getCurrentStepIndex());
        return toVO(session, recipeClient.fetchSteps(session.getRecipeId()));
    }

    /**
     * 主动完成烹饪：将进行中的会话直接置为已完成。
     *
     * @param sessionId 会话ID
     * @return 完成后的会话视图对象
     */
    @Transactional
    public SessionVO finishSession(Long sessionId) {
        KitchenSession session = loadRunningSession(sessionId);
        session.setStatus(KitchenSession.STATUS_FINISHED);
        session.setFinishedAt(LocalDateTime.now());
        sessionRepository.save(session);
        log.info("[会话] 会话{}主动完成烹饪《{}》", sessionId, session.getRecipeName());
        return toVO(session, recipeClient.fetchSteps(session.getRecipeId()));
    }

    /**
     * 放弃烹饪：将进行中的会话置为已放弃。
     *
     * @param sessionId 会话ID
     */
    @Transactional
    public void abandonSession(Long sessionId) {
        KitchenSession session = loadRunningSession(sessionId);
        session.setStatus(KitchenSession.STATUS_ABANDONED);
        sessionRepository.save(session);
        log.info("[会话] 会话{}放弃烹饪《{}》", sessionId, session.getRecipeName());
    }

    /**
     * 加载归属当前用户且仍在进行中的会话（状态机操作前置校验）。
     *
     * @param sessionId 会话ID
     * @return 进行中的会话实体
     * @throws BusinessException 越权（40300）、不存在（40400）或会话已结束（40900）
     */
    private KitchenSession loadRunningSession(Long sessionId) {
        KitchenSession session = sessionGuard.loadOwnedSession(sessionId);
        // 关键判断：已完成/已放弃的会话不允许再做状态机操作
        if (session.getStatus() != KitchenSession.STATUS_RUNNING) {
            log.warn("[会话] 会话{}已结束（状态{}），拒绝继续操作", sessionId, session.getStatus());
            throw new BusinessException(ResultCode.BIZ_CONFLICT, "烹饪会话已结束，无法继续操作");
        }
        return session;
    }

    /**
     * 计算烹饪进度百分比。
     * 算法：progress = 已完成阶段权重和 + 当前阶段权重 ×(currentStepIndex-1)/该阶段步骤数；
     * 已完成会话恒为 100。
     *
     * @param session 会话实体
     * @param byPhase 按阶段分组的步骤
     * @return 进度百分比（0-100）
     */
    public int computeProgress(KitchenSession session, Map<String, List<StepDTO>> byPhase) {
        if (session.getStatus() == KitchenSession.STATUS_FINISHED) {
            return 100;
        }
        int phaseIdx = Phases.indexOf(session.getCurrentPhase());
        if (phaseIdx < 0) {
            return 0;
        }
        // 已完成阶段：顺序在当前阶段之前的全部阶段（含被跳过的空阶段）权重求和
        int done = 0;
        for (int i = 0; i < phaseIdx; i++) {
            done += Phases.WEIGHTS.get(Phases.ORDER.get(i));
        }
        // 当前阶段：按已完成步骤数线性折算
        int phaseStepCount = byPhase.getOrDefault(session.getCurrentPhase(), List.of()).size();
        double current = 0;
        if (phaseStepCount > 0) {
            current = Phases.WEIGHTS.get(session.getCurrentPhase())
                    * (session.getCurrentStepIndex() - 1) / (double) phaseStepCount;
        }
        return (int) Math.round(done + current);
    }

    /**
     * 组装 SessionVO（含 steps 全量、currentStep、timers、progressPercent，严格按契约）。
     *
     * @param session 会话实体
     * @param steps   步骤平铺列表
     * @return 会话视图对象
     */
    public SessionVO toVO(KitchenSession session, List<StepDTO> steps) {
        Map<String, List<StepDTO>> byPhase = groupByPhase(steps);
        SessionVO vo = new SessionVO();
        vo.setId(session.getId());
        vo.setRecipeId(session.getRecipeId());
        vo.setRecipeName(session.getRecipeName());
        vo.setCurrentPhase(session.getCurrentPhase());
        vo.setCurrentStepIndex(session.getCurrentStepIndex());
        vo.setStatus(session.getStatus());
        vo.setProgressPercent(computeProgress(session, byPhase));
        vo.setCurrentStep(findCurrentStep(session, byPhase));
        vo.setSteps(steps);
        vo.setTimers(timerService.listTimerVOs(session.getId()));
        vo.setStartedAt(session.getStartedAt());
        vo.setFinishedAt(session.getFinishedAt());
        return vo;
    }

    /**
     * 在步骤列表中定位当前步骤（当前阶段 + 当前序号）。
     *
     * @param session 会话实体
     * @param byPhase 按阶段分组的步骤
     * @return 当前步骤；定位不到（如数据异常）返回 null
     */
    public StepDTO findCurrentStep(KitchenSession session, Map<String, List<StepDTO>> byPhase) {
        for (StepDTO step : byPhase.getOrDefault(session.getCurrentPhase(), List.of())) {
            if (step.getStepIndex() != null && step.getStepIndex().equals(session.getCurrentStepIndex())) {
                return step;
            }
        }
        return null;
    }

    /**
     * 按阶段分组步骤（保持 PREPARE→PLATE 顺序）。
     *
     * @param steps 步骤平铺列表
     * @return 阶段 -> 步骤列表
     */
    public Map<String, List<StepDTO>> groupByPhase(List<StepDTO> steps) {
        Map<String, List<StepDTO>> byPhase = new LinkedHashMap<>();
        for (String phase : Phases.ORDER) {
            byPhase.put(phase, steps.stream()
                    .filter(s -> phase.equals(s.getPhase()))
                    .toList());
        }
        return byPhase;
    }

    /**
     * 找到第一个有步骤的阶段（正常菜谱即 PREPARE；容错 UGC 数据缺阶段的情况）。
     *
     * @param steps 步骤平铺列表
     * @return 阶段编码
     */
    private String firstPhaseWithSteps(List<StepDTO> steps) {
        Map<String, List<StepDTO>> byPhase = groupByPhase(steps);
        for (String phase : Phases.ORDER) {
            if (!byPhase.get(phase).isEmpty()) {
                return phase;
            }
        }
        return Phases.ORDER.get(0);
    }

    /**
     * 找到当前阶段之后第一个有步骤的阶段。
     *
     * @param byPhase 按阶段分组的步骤
     * @param phase   当前阶段
     * @return 下一阶段编码；已无后续阶段返回 null（表示烹饪完成）
     */
    private String nextPhaseWithSteps(Map<String, List<StepDTO>> byPhase, String phase) {
        for (int i = Phases.indexOf(phase) + 1; i < Phases.ORDER.size(); i++) {
            String candidate = Phases.ORDER.get(i);
            if (!byPhase.getOrDefault(candidate, List.of()).isEmpty()) {
                return candidate;
            }
        }
        return null;
    }
}

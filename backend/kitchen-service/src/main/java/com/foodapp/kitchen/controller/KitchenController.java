package com.foodapp.kitchen.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.kitchen.dto.CreateSessionRequest;
import com.foodapp.kitchen.dto.CreateTimerRequest;
import com.foodapp.kitchen.dto.SessionVO;
import com.foodapp.kitchen.dto.TimerVO;
import com.foodapp.kitchen.dto.VoiceCommandRequest;
import com.foodapp.kitchen.dto.VoiceResultVO;
import com.foodapp.kitchen.service.KitchenSessionService;
import com.foodapp.kitchen.service.KitchenTimerService;
import com.foodapp.kitchen.service.KitchenVoiceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 厨房引擎接口（前缀 /api/kitchen，契约第3节，共10个接口）。
 * 鉴权说明：全部接口由 AuthInterceptor 强制登录（无白名单），
 * 当前用户通过 UserContext.requireUserId() 获取；
 * 会话归属校验（防水平越权）由 service 层 SessionGuard 统一完成。
 */
@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {

    private static final Logger log = LoggerFactory.getLogger(KitchenController.class);

    private final KitchenSessionService sessionService;
    private final KitchenTimerService timerService;
    private final KitchenVoiceService voiceService;

    public KitchenController(KitchenSessionService sessionService,
                             KitchenTimerService timerService,
                             KitchenVoiceService voiceService) {
        this.sessionService = sessionService;
        this.timerService = timerService;
        this.voiceService = voiceService;
    }

    /**
     * 开始烹饪：调菜谱服务取菜谱名与步骤，创建会话（初始为第一个有步骤的阶段第1步）。
     * 契约：POST /api/kitchen/session，req: {recipeId}，resp data: SessionVO。
     *
     * @param request 开始烹饪请求体（菜谱ID必填）
     * @return 新建会话视图对象
     */
    @PostMapping("/session")
    public Result<SessionVO> createSession(@Valid @RequestBody CreateSessionRequest request) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}请求开始烹饪, recipeId={}", userId, request.getRecipeId());
        return Result.success("开始烹饪", sessionService.createSession(request.getRecipeId()));
    }

    /**
     * 查询会话详情（含全量步骤、当前步骤、计时器列表、进度百分比）。
     * 契约：GET /api/kitchen/session/{id}，resp data: SessionVO。
     *
     * @param id 会话ID
     * @return 会话视图对象
     */
    @GetMapping("/session/{id}")
    public Result<SessionVO> getSession(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}查询会话{}", userId, id);
        return Result.success(sessionService.getSession(id));
    }

    /**
     * 下一步：阶段内推进；阶段末自动进入下一阶段；PLATE 最后一步再 next → 自动完成。
     * 契约：POST /api/kitchen/session/{id}/next，resp data: SessionVO。
     *
     * @param id 会话ID
     * @return 流转后的会话视图对象
     */
    @PostMapping("/session/{id}/next")
    public Result<SessionVO> nextStep(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}请求会话{}下一步", userId, id);
        return Result.success(sessionService.nextStep(id));
    }

    /**
     * 上一步：仅允许阶段内回退；跨阶段回退返回 40900（烹饪阶段不可逆向跳跃）。
     * 契约：POST /api/kitchen/session/{id}/prev，resp data: SessionVO。
     *
     * @param id 会话ID
     * @return 流转后的会话视图对象
     */
    @PostMapping("/session/{id}/prev")
    public Result<SessionVO> prevStep(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}请求会话{}上一步", userId, id);
        return Result.success(sessionService.prevStep(id));
    }

    /**
     * 主动完成烹饪：将进行中的会话置为已完成（status=2）。
     * 契约：POST /api/kitchen/session/{id}/finish，resp data: SessionVO。
     *
     * @param id 会话ID
     * @return 完成后的会话视图对象
     */
    @PostMapping("/session/{id}/finish")
    public Result<SessionVO> finishSession(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}请求主动完成会话{}", userId, id);
        return Result.success("烹饪完成", sessionService.finishSession(id));
    }

    /**
     * 放弃烹饪：将进行中的会话置为已放弃（status=3）。
     * 契约：POST /api/kitchen/session/{id}/abandon。
     *
     * @param id 会话ID
     * @return 成功响应（无数据）
     */
    @PostMapping("/session/{id}/abandon")
    public Result<Void> abandonSession(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}请求放弃会话{}", userId, id);
        sessionService.abandonSession(id);
        return Result.success("已放弃本次烹饪", null);
    }

    /**
     * 开启计时器：同一会话支持多个计时器并行（如焯水 + 焖煮同时计时）。
     * 契约：POST /api/kitchen/session/{id}/timer，req: {timerName,totalSec}，resp data: TimerVO。
     *
     * @param id      会话ID
     * @param request 开启计时器请求体（名称、总秒数必填）
     * @return 新建计时器视图对象
     */
    @PostMapping("/session/{id}/timer")
    public Result<TimerVO> createTimer(@PathVariable Long id, @Valid @RequestBody CreateTimerRequest request) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}为会话{}开启计时器: {}({}秒)",
                userId, id, request.getTimerName(), request.getTotalSec());
        return Result.success("计时开始", timerService.createTimer(id, request.getTimerName(), request.getTotalSec()));
    }

    /**
     * 计时器列表：查询会话下全部计时器（到点的计时中计时器自动置为已完成）。
     * 契约：GET /api/kitchen/session/{id}/timer，resp data: [TimerVO]。
     *
     * @param id 会话ID
     * @return 计时器视图列表
     */
    @GetMapping("/session/{id}/timer")
    public Result<List<TimerVO>> listTimers(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}查询会话{}计时器列表", userId, id);
        return Result.success(timerService.listTimers(id));
    }

    /**
     * 取消计时器：仅计时中的计时器允许取消（归属经由所属会话校验，防越权）。
     * 契约：POST /api/kitchen/timer/{timerId}/cancel。
     *
     * @param timerId 计时器ID
     * @return 成功响应（无数据）
     */
    @PostMapping("/timer/{timerId}/cancel")
    public Result<Void> cancelTimer(@PathVariable Long timerId) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}请求取消计时器{}", userId, timerId);
        timerService.cancelTimer(timerId);
        return Result.success("计时器已取消", null);
    }

    /**
     * 语音指令：解析指令文本并执行对应动作（每条指令落库 kitchen_voice_log）。
     * 契约：POST /api/kitchen/session/{id}/voice，req: {commandText}，
     * resp data: {parsedAction,message,session:SessionVO?}。
     * 解析规则：含"下一步/继续"→NEXT_STEP；"上一步/回退"→PREV_STEP；
     * "还有多久/几分钟"→QUERY_TIMER；"计时"→START_TIMER；其余 UNKNOWN。
     *
     * @param id      会话ID
     * @param request 语音指令请求体（指令文本必填）
     * @return 语音指令解析结果
     */
    @PostMapping("/session/{id}/voice")
    public Result<VoiceResultVO> voiceCommand(@PathVariable Long id, @Valid @RequestBody VoiceCommandRequest request) {
        Long userId = UserContext.requireUserId();
        log.info("[厨房接口] 用户{}向会话{}发送语音指令: {}", userId, id, request.getCommandText());
        return Result.success(voiceService.handleCommand(id, request.getCommandText()));
    }
}

package com.foodapp.kitchen.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.kitchen.dto.TimerVO;
import com.foodapp.kitchen.entity.KitchenSession;
import com.foodapp.kitchen.entity.KitchenTimer;
import com.foodapp.kitchen.repository.KitchenTimerRepository;
import com.foodapp.kitchen.support.SessionGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 烹饪倒计时服务。
 * 实现要点：
 * 1. 创建时计算 expected_end_at = now + totalSec，落库即视为开始计时；
 * 2. 查询时实时计算 remainSec = max(0, expectedEndAt - now)，剩余0且仍为计时中则就地置为已完成；
 * 3. @Scheduled 每5秒扫描进行中计时器做到点兜底标记（模拟推送，prod 可换 Redis 过期键通知）；
 * 4. 同一会话支持多个计时器并行（焯水 + 焖煮同时计时）。
 */
@Service
public class KitchenTimerService {

    private static final Logger log = LoggerFactory.getLogger(KitchenTimerService.class);

    private final KitchenTimerRepository timerRepository;
    private final SessionGuard sessionGuard;

    public KitchenTimerService(KitchenTimerRepository timerRepository, SessionGuard sessionGuard) {
        this.timerRepository = timerRepository;
        this.sessionGuard = sessionGuard;
    }

    /**
     * 为会话开启一个计时器（支持同一会话多个计时器并行）。
     *
     * @param sessionId 会话ID（先做归属校验）
     * @param timerName 计时器名称（如：焯水、焖煮）
     * @param totalSec  总计时秒数
     * @return 新建计时器视图对象
     */
    @Transactional
    public TimerVO createTimer(Long sessionId, String timerName, Integer totalSec) {
        KitchenSession session = sessionGuard.loadOwnedSession(sessionId);
        // 关键判断：已结束（完成/放弃）的会话不允许再开计时器
        if (session.getStatus() != KitchenSession.STATUS_RUNNING) {
            log.warn("[计时器] 会话{}已结束（状态{}），拒绝开启计时器", sessionId, session.getStatus());
            throw new BusinessException(ResultCode.BIZ_CONFLICT, "会话已结束，无法开启计时器");
        }
        LocalDateTime now = LocalDateTime.now();
        KitchenTimer timer = new KitchenTimer();
        timer.setSessionId(sessionId);
        timer.setTimerName(timerName);
        timer.setTotalSec(totalSec);
        timer.setStatus(KitchenTimer.STATUS_RUNNING);
        timer.setStartedAt(now);
        // 创建时即计算预计结束时间：now + totalSec
        timer.setExpectedEndAt(now.plusSeconds(totalSec));
        timerRepository.save(timer);
        log.info("[计时器] 会话{}开启计时器[{}]: {}, 总时长{}秒, 预计结束{}",
                sessionId, timer.getId(), timerName, totalSec, timer.getExpectedEndAt());
        return toVO(timer);
    }

    /**
     * 查询会话下全部计时器（到点的计时中计时器自动置为已完成）。
     *
     * @param sessionId 会话ID（先做归属校验）
     * @return 计时器视图列表
     */
    @Transactional
    public List<TimerVO> listTimers(Long sessionId) {
        sessionGuard.loadOwnedSession(sessionId);
        return listTimerVOs(sessionId);
    }

    /**
     * 组装会话下计时器视图列表（内部复用，不做归属校验，由调用方保证）。
     *
     * @param sessionId 会话ID
     * @return 计时器视图列表
     */
    @Transactional
    public List<TimerVO> listTimerVOs(Long sessionId) {
        List<KitchenTimer> timers = timerRepository.findBySessionIdOrderByIdAsc(sessionId);
        List<TimerVO> vos = new ArrayList<>();
        for (KitchenTimer timer : timers) {
            settleIfExpired(timer);
            vos.add(toVO(timer));
        }
        return vos;
    }

    /**
     * 查询会话下进行中的计时器（语音"还有多久"使用；查询前先做到点结算）。
     *
     * @param sessionId 会话ID
     * @return 仍在计时中的计时器列表
     */
    @Transactional
    public List<KitchenTimer> listRunningTimers(Long sessionId) {
        List<KitchenTimer> running = new ArrayList<>();
        for (KitchenTimer timer : timerRepository.findBySessionIdAndStatusOrderByIdAsc(
                sessionId, KitchenTimer.STATUS_RUNNING)) {
            settleIfExpired(timer);
            if (timer.getStatus() == KitchenTimer.STATUS_RUNNING) {
                running.add(timer);
            }
        }
        return running;
    }

    /**
     * 取消计时器（通过计时器所属会话做归属校验，防止越权取消他人计时器）。
     *
     * @param timerId 计时器ID
     */
    @Transactional
    public void cancelTimer(Long timerId) {
        KitchenTimer timer = timerRepository.findById(timerId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "计时器不存在"));
        // 归属校验：计时器挂在会话下，校验会话属于当前用户（安全要求）
        sessionGuard.loadOwnedSession(timer.getSessionId());
        // 关键判断：仅计时中的计时器允许取消
        if (timer.getStatus() != KitchenTimer.STATUS_RUNNING) {
            log.warn("[计时器] 计时器[{}]状态为{}，不可取消", timerId, timer.getStatus());
            throw new BusinessException(ResultCode.BIZ_CONFLICT, "计时器已结束，无法取消");
        }
        timer.setStatus(KitchenTimer.STATUS_CANCELLED);
        timerRepository.save(timer);
        log.info("[计时器] 计时器[{}]({})已取消", timerId, timer.getTimerName());
    }

    /**
     * 计算计时器剩余秒数：max(0, expectedEndAt - now)。
     *
     * @param timer 计时器实体
     * @return 剩余秒数（已完成/已取消返回0）
     */
    public long computeRemainSec(KitchenTimer timer) {
        if (timer.getStatus() != KitchenTimer.STATUS_RUNNING) {
            return 0L;
        }
        long remain = Duration.between(LocalDateTime.now(), timer.getExpectedEndAt()).getSeconds();
        return Math.max(0L, remain);
    }

    /**
     * 到点结算：剩余0秒且仍为计时中 → 置为已完成并落库。
     *
     * @param timer 计时器实体
     */
    private void settleIfExpired(KitchenTimer timer) {
        // 关键判断：计时到点（剩余0秒）且状态仍为计时中，自动标记完成
        if (timer.getStatus() == KitchenTimer.STATUS_RUNNING && computeRemainSec(timer) == 0L) {
            timer.setStatus(KitchenTimer.STATUS_DONE);
            timerRepository.save(timer);
            log.info("[计时器] 计时器[{}]({})到点，自动置为已完成", timer.getId(), timer.getTimerName());
        }
    }

    /**
     * 定时兜底扫描（每5秒）：将已到点但仍为计时中的计时器标记完成，模拟到点推送完成事件。
     * 说明：prod 环境可引入 Redis 过期键通知在到点瞬间推送，替代本轮询方案（见 application-prod.yml 注释）。
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void sweepExpiredTimers() {
        List<KitchenTimer> expired = timerRepository.findByStatusAndExpectedEndAtLessThanEqual(
                KitchenTimer.STATUS_RUNNING, LocalDateTime.now());
        for (KitchenTimer timer : expired) {
            timer.setStatus(KitchenTimer.STATUS_DONE);
            // 关键日志：到点事件（模拟推送）
            log.info("[计时器] 计时器[{}]({})到点，定时扫描标记完成（模拟推送完成事件）",
                    timer.getId(), timer.getTimerName());
        }
        if (!expired.isEmpty()) {
            timerRepository.saveAll(expired);
        }
    }

    /**
     * 实体转视图对象（实时计算剩余秒数）。
     *
     * @param timer 计时器实体
     * @return 计时器视图对象
     */
    public TimerVO toVO(KitchenTimer timer) {
        TimerVO vo = new TimerVO();
        vo.setId(timer.getId());
        vo.setTimerName(timer.getTimerName());
        vo.setTotalSec(timer.getTotalSec());
        vo.setRemainSec(computeRemainSec(timer));
        vo.setStatus(timer.getStatus());
        return vo;
    }
}

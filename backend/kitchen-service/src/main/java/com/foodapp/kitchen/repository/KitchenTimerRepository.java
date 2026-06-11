package com.foodapp.kitchen.repository;

import com.foodapp.kitchen.entity.KitchenTimer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 烹饪倒计时仓储。
 */
public interface KitchenTimerRepository extends JpaRepository<KitchenTimer, Long> {

    /**
     * 查询会话下全部计时器（按创建顺序）。
     *
     * @param sessionId 会话ID
     * @return 计时器列表
     */
    List<KitchenTimer> findBySessionIdOrderByIdAsc(Long sessionId);

    /**
     * 查询会话下指定状态的计时器（按创建顺序）。
     *
     * @param sessionId 会话ID
     * @param status    计时状态
     * @return 计时器列表
     */
    List<KitchenTimer> findBySessionIdAndStatusOrderByIdAsc(Long sessionId, Integer status);

    /**
     * 查询指定状态且预计结束时间早于给定时刻的计时器（定时扫描兜底用）。
     *
     * @param status   计时状态
     * @param deadline 截止时刻
     * @return 已到点的计时器列表
     */
    List<KitchenTimer> findByStatusAndExpectedEndAtLessThanEqual(Integer status, LocalDateTime deadline);
}

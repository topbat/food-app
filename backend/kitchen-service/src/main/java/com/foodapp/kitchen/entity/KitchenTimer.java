package com.foodapp.kitchen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 烹饪倒计时实体（支持同一会话多任务并行计时）。
 * 与 sql/03_kitchen.sql 中 kitchen_timer 表结构保持一致。
 */
@Entity
@Table(name = "kitchen_timer", indexes = {
        @Index(name = "idx_session_status", columnList = "session_id, status")
})
@Comment("烹饪倒计时表（多任务并行计时）")
public class KitchenTimer {

    /** 计时状态：计时中 */
    public static final int STATUS_RUNNING = 1;
    /** 计时状态：已完成 */
    public static final int STATUS_DONE = 2;
    /** 计时状态：已取消 */
    public static final int STATUS_CANCELLED = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "session_id", nullable = false)
    @Comment("关联烹饪会话ID")
    private Long sessionId;

    @Column(name = "timer_name", nullable = false, length = 50)
    @Comment("计时器名称（如：焯水、焖煮）")
    private String timerName;

    @Column(name = "total_sec", nullable = false)
    @Comment("总计时秒数")
    private Integer totalSec;

    @Column(name = "status", nullable = false)
    @Comment("计时状态（1计时中 2已完成 3已取消）")
    private Integer status = STATUS_RUNNING;

    @Column(name = "started_at", nullable = false)
    @Comment("开始计时时间")
    private LocalDateTime startedAt;

    @Column(name = "expected_end_at", nullable = false)
    @Comment("预计结束时间（到点推送完成事件）")
    private LocalDateTime expectedEndAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前填充默认时间字段。
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = now;
        }
        createdAt = now;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getTimerName() { return timerName; }
    public void setTimerName(String timerName) { this.timerName = timerName; }
    public Integer getTotalSec() { return totalSec; }
    public void setTotalSec(Integer totalSec) { this.totalSec = totalSec; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getExpectedEndAt() { return expectedEndAt; }
    public void setExpectedEndAt(LocalDateTime expectedEndAt) { this.expectedEndAt = expectedEndAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

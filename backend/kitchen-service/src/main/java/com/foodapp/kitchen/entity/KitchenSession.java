package com.foodapp.kitchen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 烹饪会话实体（厨房模式状态机）。
 * 与 sql/03_kitchen.sql 中 kitchen_session 表结构保持一致。
 */
@Entity
@Table(name = "kitchen_session", indexes = {
        @Index(name = "idx_user_status", columnList = "user_id, status")
})
@Comment("烹饪会话表（厨房模式状态机）")
public class KitchenSession {

    /** 会话状态：进行中 */
    public static final int STATUS_RUNNING = 1;
    /** 会话状态：已完成 */
    public static final int STATUS_FINISHED = 2;
    /** 会话状态：已放弃 */
    public static final int STATUS_ABANDONED = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("关联用户ID")
    private Long userId;

    @Column(name = "recipe_id", nullable = false)
    @Comment("关联菜谱ID")
    private Long recipeId;

    @Column(name = "recipe_name", nullable = false, length = 100)
    @Comment("菜谱名称（冗余存储便于展示）")
    private String recipeName;

    @Column(name = "current_phase", nullable = false, length = 10)
    @Comment("当前阶段（PREPARE/WASH/CUT/COOK/PLATE，状态不可逆跳跃）")
    private String currentPhase = "PREPARE";

    @Column(name = "current_step_index", nullable = false)
    @Comment("当前阶段内步骤序号")
    private Integer currentStepIndex = 1;

    @Column(name = "status", nullable = false)
    @Comment("会话状态（1进行中 2已完成 3已放弃）")
    private Integer status = STATUS_RUNNING;

    @Column(name = "started_at", nullable = false)
    @Comment("开始烹饪时间")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    @Comment("完成时间")
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Comment("更新时间")
    private LocalDateTime updatedAt;

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
        updatedAt = now;
    }

    /**
     * 更新前刷新更新时间。
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }
    public String getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }
    public Integer getCurrentStepIndex() { return currentStepIndex; }
    public void setCurrentStepIndex(Integer currentStepIndex) { this.currentStepIndex = currentStepIndex; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.foodapp.recipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 菜谱结构化步骤表实体（对应表 recipe_step）。
 * 五步法阶段：PREPARE前期准备 / WASH洗 / CUT切 / COOK煮 / PLATE装盘。
 */
@Entity
@Table(name = "recipe_step")
@Comment("菜谱结构化步骤表（五步法）")
public class RecipeStep {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    /** 关联菜谱ID */
    @Column(name = "recipe_id", nullable = false)
    @Comment("关联菜谱ID")
    private Long recipeId;

    /** 阶段（PREPARE前期准备 WASH洗 CUT切 COOK煮 PLATE装盘） */
    @Column(name = "phase", nullable = false, length = 10)
    @Comment("阶段（PREPARE前期准备 WASH洗 CUT切 COOK煮 PLATE装盘）")
    private String phase;

    /** 阶段内排序序号（从1开始） */
    @Column(name = "step_index", nullable = false)
    @Comment("阶段内排序序号（从1开始）")
    private Integer stepIndex = 1;

    /** 动作标题（如：切丁、热锅倒油） */
    @Column(name = "action_title", nullable = false, length = 50)
    @Comment("动作标题（如：切丁、热锅倒油）")
    private String actionTitle;

    /** 详细图文描述 */
    @Column(name = "detail", columnDefinition = "TEXT")
    @Comment("详细图文描述")
    private String detail;

    /** 动图/短视频URL（竖屏短视频或GIF） */
    @Column(name = "media_url", length = 255)
    @Comment("动图/短视频URL（竖屏短视频或GIF）")
    private String mediaUrl;

    /** 计时秒数（0表示该步骤无需计时） */
    @Column(name = "timer_sec", nullable = false)
    @Comment("计时秒数（0表示该步骤无需计时）")
    private Integer timerSec = 0;

    /** 火候（大火/中火/小火，仅COOK阶段） */
    @Column(name = "fire_power", length = 20)
    @Comment("火候（大火/中火/小火，仅COOK阶段）")
    private String firePower;

    /** 创建时间 */
    @Column(name = "created_at", nullable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充创建时间。
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public Integer getStepIndex() { return stepIndex; }
    public void setStepIndex(Integer stepIndex) { this.stepIndex = stepIndex; }
    public String getActionTitle() { return actionTitle; }
    public void setActionTitle(String actionTitle) { this.actionTitle = actionTitle; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public Integer getTimerSec() { return timerSec; }
    public void setTimerSec(Integer timerSec) { this.timerSec = timerSec; }
    public String getFirePower() { return firePower; }
    public void setFirePower(String firePower) { this.firePower = firePower; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

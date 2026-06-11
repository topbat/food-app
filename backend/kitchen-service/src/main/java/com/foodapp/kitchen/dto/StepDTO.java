package com.foodapp.kitchen.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 菜谱步骤 DTO。
 * 字段与菜谱服务契约 Step 一致：
 * {id,recipeId,phase,stepIndex,actionTitle,detail,mediaUrl,timerSec,firePower}。
 * 既用于反序列化菜谱服务响应，也原样输出到 SessionVO.steps / currentStep。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StepDTO {

    /** 步骤ID */
    private Long id;
    /** 关联菜谱ID */
    private Long recipeId;
    /** 阶段（PREPARE/WASH/CUT/COOK/PLATE） */
    private String phase;
    /** 阶段内排序序号（从1开始） */
    private Integer stepIndex;
    /** 动作标题（如：切丁、热锅倒油） */
    private String actionTitle;
    /** 详细图文描述 */
    private String detail;
    /** 动图/短视频URL */
    private String mediaUrl;
    /** 计时秒数（0表示该步骤无需计时） */
    private Integer timerSec;
    /** 火候（大火/中火/小火，仅COOK阶段） */
    private String firePower;

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
}

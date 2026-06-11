package com.foodapp.recipe.vo;

/**
 * 菜谱步骤 VO（字段与接口契约 Step 一致，厨房服务与前端共用）。
 */
public class StepVO {

    /** 步骤ID */
    private Long id;
    /** 菜谱ID */
    private Long recipeId;
    /** 阶段（PREPARE/WASH/CUT/COOK/PLATE） */
    private String phase;
    /** 阶段内序号 */
    private Integer stepIndex;
    /** 动作标题 */
    private String actionTitle;
    /** 详细描述 */
    private String detail;
    /** 动图/短视频URL */
    private String mediaUrl;
    /** 计时秒数（0表示无需计时） */
    private Integer timerSec;
    /** 火候（仅COOK阶段） */
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

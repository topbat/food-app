package com.foodapp.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * UGC 菜谱上传请求（五步法标准化模板）。
 * 业务校验：steps 必须覆盖 PREPARE/WASH/CUT/COOK/PLATE 全部 5 个阶段。
 */
public class UgcRecipeRequest {

    /** 菜谱名称 */
    @NotBlank(message = "菜谱名称不能为空")
    @Size(max = 100, message = "菜谱名称不能超过100字")
    private String title;

    /** 封面图URL（可选） */
    private String coverUrl;

    /** 菜系（1-9） */
    @NotNull(message = "菜系不能为空")
    @Min(value = 1, message = "菜系编码必须在1-9之间")
    @Max(value = 9, message = "菜系编码必须在1-9之间")
    private Integer cuisineType;

    /** 难度（1-3） */
    @NotNull(message = "难度不能为空")
    @Min(value = 1, message = "难度必须在1-3之间")
    @Max(value = 3, message = "难度必须在1-3之间")
    private Integer difficulty;

    /** 总耗时（分钟） */
    @NotNull(message = "总耗时不能为空")
    @Min(value = 1, message = "总耗时至少1分钟")
    private Integer totalTimeMin;

    /** 份数 */
    @NotNull(message = "份数不能为空")
    @Min(value = 1, message = "份数至少为1")
    private Integer servings;

    /** 菜谱简介（可选） */
    @Size(max = 500, message = "简介不能超过500字")
    private String description;

    /** 小贴士（可选） */
    @Size(max = 500, message = "小贴士不能超过500字")
    private String tips;

    /** 食材列表 */
    @NotEmpty(message = "食材列表不能为空")
    @Valid
    private List<IngredientItem> ingredients;

    /** 五步法步骤列表 */
    @NotEmpty(message = "步骤列表不能为空")
    @Valid
    private List<StepItem> steps;

    /** 适宜标签ID列表（可选） */
    private List<Long> suitableTagIds;

    /** 慎用标签ID列表（可选） */
    private List<Long> unsuitableTagIds;

    /**
     * 食材项。
     */
    public static class IngredientItem {
        /** 基础食材库ID */
        @NotNull(message = "食材ID不能为空")
        private Long ingredientId;
        /** 用量数值 */
        @NotNull(message = "食材用量不能为空")
        private BigDecimal amount;
        /** 单位 */
        @NotBlank(message = "食材单位不能为空")
        private String unit;
        /** 是否核心食材（0可选 1必须） */
        private Integer isEssential = 1;

        public Long getIngredientId() { return ingredientId; }
        public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Integer getIsEssential() { return isEssential; }
        public void setIsEssential(Integer isEssential) { this.isEssential = isEssential; }
    }

    /**
     * 步骤项。
     */
    public static class StepItem {
        /** 阶段（PREPARE/WASH/CUT/COOK/PLATE） */
        @NotBlank(message = "步骤阶段不能为空")
        private String phase;
        /** 阶段内序号 */
        @NotNull(message = "步骤序号不能为空")
        @Min(value = 1, message = "步骤序号从1开始")
        private Integer stepIndex;
        /** 动作标题 */
        @NotBlank(message = "动作标题不能为空")
        @Size(max = 50, message = "动作标题不能超过50字")
        private String actionTitle;
        /** 详细描述 */
        @NotBlank(message = "步骤描述不能为空")
        private String detail;
        /** 动图/短视频URL（可选） */
        private String mediaUrl;
        /** 计时秒数（可选，默认0） */
        private Integer timerSec = 0;
        /** 火候（可选，仅COOK阶段） */
        private String firePower;

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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getCuisineType() { return cuisineType; }
    public void setCuisineType(Integer cuisineType) { this.cuisineType = cuisineType; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public Integer getTotalTimeMin() { return totalTimeMin; }
    public void setTotalTimeMin(Integer totalTimeMin) { this.totalTimeMin = totalTimeMin; }
    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }
    public List<IngredientItem> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientItem> ingredients) { this.ingredients = ingredients; }
    public List<StepItem> getSteps() { return steps; }
    public void setSteps(List<StepItem> steps) { this.steps = steps; }
    public List<Long> getSuitableTagIds() { return suitableTagIds; }
    public void setSuitableTagIds(List<Long> suitableTagIds) { this.suitableTagIds = suitableTagIds; }
    public List<Long> getUnsuitableTagIds() { return unsuitableTagIds; }
    public void setUnsuitableTagIds(List<Long> unsuitableTagIds) { this.unsuitableTagIds = unsuitableTagIds; }
}

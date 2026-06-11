package com.foodapp.kitchen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 烹饪会话视图对象。
 * 契约：SessionVO = {id,recipeId,recipeName,currentPhase,currentStepIndex,status,
 * progressPercent,currentStep:Step,steps:[Step],timers:[TimerVO],startedAt,finishedAt}。
 */
public class SessionVO {

    /** 会话ID */
    private Long id;
    /** 关联菜谱ID */
    private Long recipeId;
    /** 菜谱名称 */
    private String recipeName;
    /** 当前阶段（PREPARE/WASH/CUT/COOK/PLATE） */
    private String currentPhase;
    /** 当前阶段内步骤序号 */
    private Integer currentStepIndex;
    /** 会话状态（1进行中 2已完成 3已放弃） */
    private Integer status;
    /** 烹饪进度百分比（0-100） */
    private Integer progressPercent;
    /** 当前步骤 */
    private StepDTO currentStep;
    /** 全量步骤平铺列表（按阶段顺序） */
    private List<StepDTO> steps;
    /** 会话下计时器列表 */
    private List<TimerVO> timers;
    /** 开始烹饪时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startedAt;
    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime finishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
    public StepDTO getCurrentStep() { return currentStep; }
    public void setCurrentStep(StepDTO currentStep) { this.currentStep = currentStep; }
    public List<StepDTO> getSteps() { return steps; }
    public void setSteps(List<StepDTO> steps) { this.steps = steps; }
    public List<TimerVO> getTimers() { return timers; }
    public void setTimers(List<TimerVO> timers) { this.timers = timers; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
}

package com.foodapp.recipe.vo;

import java.util.List;
import java.util.Map;

/**
 * 菜谱详情聚合 VO：基本信息 + 五步法分组步骤 + 食材清单 + 适宜/慎用标签 + 阶段进度权重。
 */
public class RecipeDetailVO {

    /** 菜谱基本信息（含 tips/servings/sourceType） */
    private RecipeSummaryVO info;
    /** 五步法分组步骤（键固定顺序 PREPARE→WASH→CUT→COOK→PLATE） */
    private Map<String, List<StepVO>> stepsByPhase;
    /** 食材清单 */
    private List<IngredientVO> ingredients;
    /** 适宜人群/功效标签 */
    private List<String> suitableTags;
    /** 慎用/禁忌标签 */
    private List<String> unsuitableTags;
    /** 阶段进度权重（准备10/洗5/切15/煮60/装盘10） */
    private Map<String, Integer> phaseWeights;

    public RecipeSummaryVO getInfo() { return info; }
    public void setInfo(RecipeSummaryVO info) { this.info = info; }
    public Map<String, List<StepVO>> getStepsByPhase() { return stepsByPhase; }
    public void setStepsByPhase(Map<String, List<StepVO>> stepsByPhase) { this.stepsByPhase = stepsByPhase; }
    public List<IngredientVO> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientVO> ingredients) { this.ingredients = ingredients; }
    public List<String> getSuitableTags() { return suitableTags; }
    public void setSuitableTags(List<String> suitableTags) { this.suitableTags = suitableTags; }
    public List<String> getUnsuitableTags() { return unsuitableTags; }
    public void setUnsuitableTags(List<String> unsuitableTags) { this.unsuitableTags = unsuitableTags; }
    public Map<String, Integer> getPhaseWeights() { return phaseWeights; }
    public void setPhaseWeights(Map<String, Integer> phaseWeights) { this.phaseWeights = phaseWeights; }
}

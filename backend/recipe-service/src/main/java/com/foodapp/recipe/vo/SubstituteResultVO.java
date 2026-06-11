package com.foodapp.recipe.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 智能食材替换结果 VO：替换前后热量对比 + 替换明细。
 */
public class SubstituteResultVO {

    /** 替换前单人份热量（kcal） */
    private BigDecimal originalCalories;
    /** 替换后单人份热量（kcal） */
    private BigDecimal newCalories;
    /** 热量差（正数表示减少的kcal） */
    private BigDecimal calorieDiff;
    /** 替换明细列表 */
    private List<SubstitutionItemVO> substitutions;

    public BigDecimal getOriginalCalories() { return originalCalories; }
    public void setOriginalCalories(BigDecimal originalCalories) { this.originalCalories = originalCalories; }
    public BigDecimal getNewCalories() { return newCalories; }
    public void setNewCalories(BigDecimal newCalories) { this.newCalories = newCalories; }
    public BigDecimal getCalorieDiff() { return calorieDiff; }
    public void setCalorieDiff(BigDecimal calorieDiff) { this.calorieDiff = calorieDiff; }
    public List<SubstitutionItemVO> getSubstitutions() { return substitutions; }
    public void setSubstitutions(List<SubstitutionItemVO> substitutions) { this.substitutions = substitutions; }

    /**
     * 单条替换明细。
     */
    public static class SubstitutionItemVO {
        /** 原食材名称 */
        private String sourceName;
        /** 替换为食材名称 */
        private String targetName;
        /** 替换理由 */
        private String reason;
        /** 该项替换减少的热量（kcal，正数为减少） */
        private BigDecimal calorieDiff;

        public String getSourceName() { return sourceName; }
        public void setSourceName(String sourceName) { this.sourceName = sourceName; }
        public String getTargetName() { return targetName; }
        public void setTargetName(String targetName) { this.targetName = targetName; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public BigDecimal getCalorieDiff() { return calorieDiff; }
        public void setCalorieDiff(BigDecimal calorieDiff) { this.calorieDiff = calorieDiff; }
    }
}

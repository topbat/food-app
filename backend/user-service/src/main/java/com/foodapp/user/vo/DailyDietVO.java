package com.foodapp.user.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 当日饮食统计响应：记录列表 + 营养汇总 + 热量目标对比。
 */
public class DailyDietVO {

    /** 当日饮食记录列表 */
    private List<DietRecordVO> records;
    /** 当日总热量（kcal） */
    private BigDecimal totalCalories;
    /** 当日总碳水（克） */
    private BigDecimal totalCarbs;
    /** 当日总蛋白质（克） */
    private BigDecimal totalProtein;
    /** 当日总脂肪（克） */
    private BigDecimal totalFat;
    /** 每日热量目标（kcal，未设置时为 null） */
    private Integer calorieTarget;
    /** 是否超过热量目标（未设置目标时恒为 false） */
    private Boolean exceedTarget;

    public List<DietRecordVO> getRecords() { return records; }
    public void setRecords(List<DietRecordVO> records) { this.records = records; }
    public BigDecimal getTotalCalories() { return totalCalories; }
    public void setTotalCalories(BigDecimal totalCalories) { this.totalCalories = totalCalories; }
    public BigDecimal getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(BigDecimal totalCarbs) { this.totalCarbs = totalCarbs; }
    public BigDecimal getTotalProtein() { return totalProtein; }
    public void setTotalProtein(BigDecimal totalProtein) { this.totalProtein = totalProtein; }
    public BigDecimal getTotalFat() { return totalFat; }
    public void setTotalFat(BigDecimal totalFat) { this.totalFat = totalFat; }
    public Integer getCalorieTarget() { return calorieTarget; }
    public void setCalorieTarget(Integer calorieTarget) { this.calorieTarget = calorieTarget; }
    public Boolean getExceedTarget() { return exceedTarget; }
    public void setExceedTarget(Boolean exceedTarget) { this.exceedTarget = exceedTarget; }

    /**
     * 当日单条饮食记录VO。
     */
    public static class DietRecordVO {

        /** 记录ID */
        private Long id;
        /** 餐次（1早餐 2午餐 3晚餐 4加餐） */
        private Integer mealType;
        /** 菜品名称 */
        private String recipeName;
        /** 摄入热量（kcal） */
        private BigDecimal caloriesKcal;
        /** 碳水化合物（克） */
        private BigDecimal carbsG;
        /** 蛋白质（克） */
        private BigDecimal proteinG;
        /** 脂肪（克） */
        private BigDecimal fatG;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getMealType() { return mealType; }
        public void setMealType(Integer mealType) { this.mealType = mealType; }
        public String getRecipeName() { return recipeName; }
        public void setRecipeName(String recipeName) { this.recipeName = recipeName; }
        public BigDecimal getCaloriesKcal() { return caloriesKcal; }
        public void setCaloriesKcal(BigDecimal caloriesKcal) { this.caloriesKcal = caloriesKcal; }
        public BigDecimal getCarbsG() { return carbsG; }
        public void setCarbsG(BigDecimal carbsG) { this.carbsG = carbsG; }
        public BigDecimal getProteinG() { return proteinG; }
        public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }
        public BigDecimal getFatG() { return fatG; }
        public void setFatG(BigDecimal fatG) { this.fatG = fatG; }
    }
}

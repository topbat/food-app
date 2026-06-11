package com.foodapp.user.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 记录饮食请求入参。
 */
public class DietRecordRequest {

    /** 记录日期（格式 yyyy-MM-dd） */
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    /** 餐次（1早餐 2午餐 3晚餐 4加餐） */
    @NotNull(message = "餐次不能为空")
    @Min(value = 1, message = "餐次取值范围为1~4")
    @Max(value = 4, message = "餐次取值范围为1~4")
    private Integer mealType;

    /** 关联菜谱ID（可空，支持手动记录） */
    private Long recipeId;

    /** 菜品名称 */
    @NotBlank(message = "菜品名称不能为空")
    @Size(max = 100, message = "菜品名称长度不能超过100个字符")
    private String recipeName;

    /** 摄入热量（kcal） */
    @NotNull(message = "摄入热量不能为空")
    @DecimalMin(value = "0", message = "摄入热量不能为负数")
    private BigDecimal caloriesKcal;

    /** 碳水化合物（克，可空默认0） */
    @DecimalMin(value = "0", message = "碳水化合物不能为负数")
    private BigDecimal carbsG;

    /** 蛋白质（克，可空默认0） */
    @DecimalMin(value = "0", message = "蛋白质不能为负数")
    private BigDecimal proteinG;

    /** 脂肪（克，可空默认0） */
    @DecimalMin(value = "0", message = "脂肪不能为负数")
    private BigDecimal fatG;

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Integer getMealType() { return mealType; }
    public void setMealType(Integer mealType) { this.mealType = mealType; }
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
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

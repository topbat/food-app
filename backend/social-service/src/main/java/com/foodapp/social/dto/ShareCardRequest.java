package com.foodapp.social.dto;

/**
 * 分享卡数据请求体。
 */
public class ShareCardRequest {

    /** 关联菜谱ID（可空） */
    private Long recipeId;

    /** 菜谱名称（可空，用于组装文案） */
    private String recipeName;

    /** 本餐热量（千卡，可空） */
    private Double caloriesKcal;

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }
    public Double getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(Double caloriesKcal) { this.caloriesKcal = caloriesKcal; }
}

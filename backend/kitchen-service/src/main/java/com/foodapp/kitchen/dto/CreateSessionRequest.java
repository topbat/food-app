package com.foodapp.kitchen.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 开始烹饪请求体。
 */
public class CreateSessionRequest {

    /** 菜谱ID */
    @NotNull(message = "菜谱ID不能为空")
    private Long recipeId;

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
}

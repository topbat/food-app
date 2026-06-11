package com.foodapp.social.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 菜谱评分请求体。
 */
public class RatingRequest {

    /** 菜谱ID */
    @NotNull(message = "菜谱ID不能为空")
    private Long recipeId;

    /** 评分（1-5星） */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer score;

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}

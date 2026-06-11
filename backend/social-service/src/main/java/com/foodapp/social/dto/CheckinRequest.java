package com.foodapp.social.dto;

import jakarta.validation.constraints.Size;

/**
 * 打卡请求体。
 */
public class CheckinRequest {

    /** 关联菜谱ID（可空） */
    private Long recipeId;

    /** 打卡备注（可空） */
    @Size(max = 200, message = "打卡备注最长200字")
    private String note;

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

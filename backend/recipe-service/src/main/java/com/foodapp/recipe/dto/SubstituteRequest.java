package com.foodapp.recipe.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 智能替换请求：指定适用场景。
 */
public class SubstituteRequest {

    /** 适用场景（减脂/控糖/素食/低嘌呤） */
    @NotBlank(message = "场景不能为空")
    private String scene;

    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
}

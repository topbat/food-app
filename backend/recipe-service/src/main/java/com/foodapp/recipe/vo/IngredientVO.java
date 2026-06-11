package com.foodapp.recipe.vo;

import java.math.BigDecimal;

/**
 * 菜谱食材 VO（详情页食材清单项）。
 */
public class IngredientVO {

    /** 关联记录ID */
    private Long id;
    /** 基础食材库ID */
    private Long ingredientId;
    /** 食材名称 */
    private String ingredientName;
    /** 用量数值 */
    private BigDecimal amount;
    /** 单位（克/毫升/个/勺） */
    private String unit;
    /** 是否核心食材（0可选替换 1必须） */
    private Integer isEssential;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIngredientId() { return ingredientId; }
    public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getIsEssential() { return isEssential; }
    public void setIsEssential(Integer isEssential) { this.isEssential = isEssential; }
}

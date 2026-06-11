package com.foodapp.recipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜谱食材关联表实体（对应表 recipe_ingredient）。
 * 记录某菜谱使用的食材与精确用量；ingredient_name 冗余存储便于展示与冰箱清理模式匹配。
 */
@Entity
@Table(name = "recipe_ingredient")
@Comment("菜谱食材关联表")
public class RecipeIngredient {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    /** 关联菜谱ID */
    @Column(name = "recipe_id", nullable = false)
    @Comment("关联菜谱ID")
    private Long recipeId;

    /** 关联基础食材库ID */
    @Column(name = "ingredient_id", nullable = false)
    @Comment("关联基础食材库ID")
    private Long ingredientId;

    /** 食材名称（冗余存储便于展示） */
    @Column(name = "ingredient_name", nullable = false, length = 50)
    @Comment("食材名称（冗余存储便于展示）")
    private String ingredientName;

    /** 用量数值 */
    @Column(name = "amount", nullable = false, precision = 6, scale = 2)
    @Comment("用量数值")
    private BigDecimal amount;

    /** 单位（克/毫升/个/勺） */
    @Column(name = "unit", nullable = false, length = 10)
    @Comment("单位（克/毫升/个/勺）")
    private String unit = "克";

    /** 是否核心食材（0可选替换 1必须） */
    @Column(name = "is_essential", nullable = false)
    @Comment("是否核心食材（0可选替换 1必须）")
    private Integer isEssential = 1;

    /** 创建时间 */
    @Column(name = "created_at", nullable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充创建时间。
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

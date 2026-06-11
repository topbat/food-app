package com.foodapp.recipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 基础食材库表实体（对应表 recipe_ingredient_lib）。
 * 存储每100克的热量与三大营养素，是营养计算与智能替换的数据基础。
 */
@Entity
@Table(name = "recipe_ingredient_lib", uniqueConstraints = @UniqueConstraint(name = "uk_name", columnNames = "name"))
@Comment("基础食材库表（含营养成分）")
public class RecipeIngredientLib {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    /** 食材名称（如：鸡胸肉） */
    @Column(name = "name", nullable = false, length = 50)
    @Comment("食材名称（如：鸡胸肉）")
    private String name;

    /** 食材分类（肉类/蔬菜/调料/主食等） */
    @Column(name = "category", length = 20)
    @Comment("食材分类（肉类/蔬菜/调料/主食等）")
    private String category;

    /** 每100克热量（kcal） */
    @Column(name = "calories_per_100g", nullable = false, precision = 7, scale = 2)
    @Comment("每100克热量（kcal）")
    private BigDecimal caloriesPer100g = BigDecimal.ZERO;

    /** 每100克碳水化合物（克） */
    @Column(name = "carbs_per_100g", nullable = false, precision = 6, scale = 2)
    @Comment("每100克碳水化合物（克）")
    private BigDecimal carbsPer100g = BigDecimal.ZERO;

    /** 每100克蛋白质（克） */
    @Column(name = "protein_per_100g", nullable = false, precision = 6, scale = 2)
    @Comment("每100克蛋白质（克）")
    private BigDecimal proteinPer100g = BigDecimal.ZERO;

    /** 每100克脂肪（克） */
    @Column(name = "fat_per_100g", nullable = false, precision = 6, scale = 2)
    @Comment("每100克脂肪（克）")
    private BigDecimal fatPer100g = BigDecimal.ZERO;

    /** 禁忌提示（如：嘌呤较高，痛风人群慎食） */
    @Column(name = "taboo_note", length = 200)
    @Comment("禁忌提示（如：嘌呤较高，痛风人群慎食）")
    private String tabooNote;

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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getCaloriesPer100g() { return caloriesPer100g; }
    public void setCaloriesPer100g(BigDecimal caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }
    public BigDecimal getCarbsPer100g() { return carbsPer100g; }
    public void setCarbsPer100g(BigDecimal carbsPer100g) { this.carbsPer100g = carbsPer100g; }
    public BigDecimal getProteinPer100g() { return proteinPer100g; }
    public void setProteinPer100g(BigDecimal proteinPer100g) { this.proteinPer100g = proteinPer100g; }
    public BigDecimal getFatPer100g() { return fatPer100g; }
    public void setFatPer100g(BigDecimal fatPer100g) { this.fatPer100g = fatPer100g; }
    public String getTabooNote() { return tabooNote; }
    public void setTabooNote(String tabooNote) { this.tabooNote = tabooNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.foodapp.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 饮食日历记录实体（每日能量摄入统计），对应表 user_diet_record。
 */
@Entity
@Table(name = "user_diet_record")
@Comment("饮食日历记录表（每日能量摄入统计）")
public class UserDietRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("关联用户ID")
    private Long userId;

    @Column(name = "record_date", nullable = false)
    @Comment("记录日期")
    private LocalDate recordDate;

    @Column(name = "meal_type", nullable = false)
    @Comment("餐次（1早餐 2午餐 3晚餐 4加餐）")
    private Integer mealType;

    @Column(name = "recipe_id")
    @Comment("关联菜谱ID（可空，支持手动记录）")
    private Long recipeId;

    @Column(name = "recipe_name", nullable = false, length = 100)
    @Comment("菜品名称（冗余存储便于展示）")
    private String recipeName;

    @Column(name = "calories_kcal", nullable = false, precision = 7, scale = 2)
    @Comment("摄入热量（kcal）")
    private BigDecimal caloriesKcal;

    @Column(name = "carbs_g", precision = 6, scale = 2)
    @Comment("碳水化合物（克）")
    private BigDecimal carbsG;

    @Column(name = "protein_g", precision = 6, scale = 2)
    @Comment("蛋白质（克）")
    private BigDecimal proteinG;

    @Column(name = "fat_g", precision = 6, scale = 2)
    @Comment("脂肪（克）")
    private BigDecimal fatG;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充创建时间与营养素默认值（0）。
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.caloriesKcal == null) {
            this.caloriesKcal = BigDecimal.ZERO;
        }
        if (this.carbsG == null) {
            this.carbsG = BigDecimal.ZERO;
        }
        if (this.proteinG == null) {
            this.proteinG = BigDecimal.ZERO;
        }
        if (this.fatG == null) {
            this.fatG = BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

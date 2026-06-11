package com.foodapp.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户健康档案实体，对应表 user_health_profile。
 */
@Entity
@Table(name = "user_health_profile")
@Comment("用户健康档案表")
public class UserHealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    @Comment("关联用户ID")
    private Long userId;

    @Column(name = "height_cm", precision = 5, scale = 1)
    @Comment("身高（厘米）")
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    @Comment("体重（千克）")
    private BigDecimal weightKg;

    @Column(name = "allergy_history", length = 500)
    @Comment("过敏史（如：海鲜、花生，逗号分隔）")
    private String allergyHistory;

    @Column(name = "diet_preference", length = 200)
    @Comment("饮食偏好（如：少辣、素食）")
    private String dietPreference;

    @Column(name = "health_goal", length = 20)
    @Comment("健康目标（减脂/增肌/控糖/均衡）")
    private String healthGoal;

    @Column(name = "daily_calorie_target")
    @Comment("每日摄入热量目标（kcal）")
    private Integer dailyCalorieTarget;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Comment("更新时间")
    private LocalDateTime updatedAt;

    /**
     * 持久化前自动填充创建/更新时间。
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 更新前自动刷新更新时间。
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public String getAllergyHistory() { return allergyHistory; }
    public void setAllergyHistory(String allergyHistory) { this.allergyHistory = allergyHistory; }
    public String getDietPreference() { return dietPreference; }
    public void setDietPreference(String dietPreference) { this.dietPreference = dietPreference; }
    public String getHealthGoal() { return healthGoal; }
    public void setHealthGoal(String healthGoal) { this.healthGoal = healthGoal; }
    public Integer getDailyCalorieTarget() { return dailyCalorieTarget; }
    public void setDailyCalorieTarget(Integer dailyCalorieTarget) { this.dailyCalorieTarget = dailyCalorieTarget; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.foodapp.user.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 更新健康档案请求入参（全部字段可选，仅更新传入的字段）。
 */
public class UpdateProfileRequest {

    /** 昵称（可选） */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /** 头像URL（可选） */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatarUrl;

    /** 身高（厘米，可选） */
    @DecimalMin(value = "0", inclusive = false, message = "身高必须大于0")
    private BigDecimal heightCm;

    /** 体重（千克，可选） */
    @DecimalMin(value = "0", inclusive = false, message = "体重必须大于0")
    private BigDecimal weightKg;

    /** 过敏史（可选） */
    @Size(max = 500, message = "过敏史长度不能超过500个字符")
    private String allergyHistory;

    /** 饮食偏好（可选） */
    @Size(max = 200, message = "饮食偏好长度不能超过200个字符")
    private String dietPreference;

    /** 健康目标（减脂/增肌/控糖/均衡，可选） */
    @Size(max = 20, message = "健康目标长度不能超过20个字符")
    private String healthGoal;

    /** 每日摄入热量目标（kcal，可选） */
    @Min(value = 1, message = "每日热量目标必须大于0")
    private Integer dailyCalorieTarget;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
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
}

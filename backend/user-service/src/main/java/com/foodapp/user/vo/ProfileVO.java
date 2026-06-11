package com.foodapp.user.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 个人主页响应：用户信息 + 健康档案 + 标签列表。
 */
public class ProfileVO {

    /** 用户信息 */
    private ProfileUserVO user;
    /** 健康档案 */
    private HealthProfileVO profile;
    /** 标签列表 */
    private List<TagVO> tags;

    public ProfileUserVO getUser() { return user; }
    public void setUser(ProfileUserVO user) { this.user = user; }
    public HealthProfileVO getProfile() { return profile; }
    public void setProfile(HealthProfileVO profile) { this.profile = profile; }
    public List<TagVO> getTags() { return tags; }
    public void setTags(List<TagVO> tags) { this.tags = tags; }

    /**
     * 个人主页中的用户信息（含性别与出生日期，不含密码）。
     */
    public static class ProfileUserVO {

        /** 用户ID */
        private Long id;
        /** 登录用户名 */
        private String username;
        /** 昵称 */
        private String nickname;
        /** 头像URL */
        private String avatarUrl;
        /** 性别（0未知 1男 2女） */
        private Integer gender;
        /** 出生日期 */
        private LocalDate birthDate;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public Integer getGender() { return gender; }
        public void setGender(Integer gender) { this.gender = gender; }
        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    }

    /**
     * 个人主页中的健康档案信息。
     */
    public static class HealthProfileVO {

        /** 身高（厘米） */
        private BigDecimal heightCm;
        /** 体重（千克） */
        private BigDecimal weightKg;
        /** 过敏史 */
        private String allergyHistory;
        /** 饮食偏好 */
        private String dietPreference;
        /** 健康目标（减脂/增肌/控糖/均衡） */
        private String healthGoal;
        /** 每日摄入热量目标（kcal） */
        private Integer dailyCalorieTarget;

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
}

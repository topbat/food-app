package com.foodapp.social.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 烹饪打卡实体，对应表 social_checkin（同一用户同一天仅可打卡一次，连续天数写入时计算）。
 */
@Entity
@Table(name = "social_checkin", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_date", columnNames = {"user_id", "checkin_date"})
})
@Comment("烹饪打卡表（连续打卡统计）")
public class SocialCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("打卡用户ID")
    private Long userId;

    @Column(name = "checkin_date", nullable = false)
    @Comment("打卡日期")
    private LocalDate checkinDate;

    @Column(name = "recipe_id")
    @Comment("关联菜谱ID（可空）")
    private Long recipeId;

    @Column(name = "note", length = 200)
    @Comment("打卡备注")
    private String note;

    @Column(name = "continuous_days", nullable = false)
    @Comment("当前连续打卡天数（写入时计算）")
    private Integer continuousDays;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充创建时间与默认值。
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.continuousDays == null) {
            this.continuousDays = 1;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getCheckinDate() { return checkinDate; }
    public void setCheckinDate(LocalDate checkinDate) { this.checkinDate = checkinDate; }
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getContinuousDays() { return continuousDays; }
    public void setContinuousDays(Integer continuousDays) { this.continuousDays = continuousDays; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

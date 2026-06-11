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

import java.time.LocalDateTime;

/**
 * 用户已获徽章实体，对应表 social_user_badge（同一徽章同一用户仅可获得一次）。
 */
@Entity
@Table(name = "social_user_badge", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_badge", columnNames = {"user_id", "badge_id"})
})
@Comment("用户已获徽章表")
public class SocialUserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("用户ID")
    private Long userId;

    @Column(name = "badge_id", nullable = false)
    @Comment("徽章ID")
    private Long badgeId;

    @Column(name = "obtained_at", nullable = false, updatable = false)
    @Comment("获得时间")
    private LocalDateTime obtainedAt;

    /**
     * 持久化前自动填充获得时间。
     */
    @PrePersist
    public void prePersist() {
        this.obtainedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBadgeId() { return badgeId; }
    public void setBadgeId(Long badgeId) { this.badgeId = badgeId; }
    public LocalDateTime getObtainedAt() { return obtainedAt; }
    public void setObtainedAt(LocalDateTime obtainedAt) { this.obtainedAt = obtainedAt; }
}

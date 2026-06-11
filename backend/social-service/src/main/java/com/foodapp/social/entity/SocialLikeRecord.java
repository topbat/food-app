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
 * 点赞记录实体，对应表 social_like_record（唯一约束防重复点赞，点赞toggle依据）。
 */
@Entity
@Table(name = "social_like_record", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_target", columnNames = {"user_id", "target_type", "target_id"})
})
@Comment("点赞记录表（唯一约束防重复点赞）")
public class SocialLikeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("点赞用户ID")
    private Long userId;

    @Column(name = "target_type", nullable = false)
    @Comment("点赞对象类型（1帖子 2评论 3菜谱）")
    private Integer targetType;

    @Column(name = "target_id", nullable = false)
    @Comment("点赞对象ID")
    private Long targetId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充创建时间。
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

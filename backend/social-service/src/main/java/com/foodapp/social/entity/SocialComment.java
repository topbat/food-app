package com.foodapp.social.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 评论实体，对应表 social_comment（支持针对菜谱整体或某一步骤的评论，支持楼中楼回复）。
 */
@Entity
@Table(name = "social_comment", indexes = {
        @Index(name = "idx_target", columnList = "target_type, target_id")
})
@Comment("评论表（支持菜谱步骤级评论）")
public class SocialComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "target_type", nullable = false)
    @Comment("评论对象类型（1菜谱 2社区帖子）")
    private Integer targetType;

    @Column(name = "target_id", nullable = false)
    @Comment("评论对象ID（菜谱ID或帖子ID）")
    private Long targetId;

    @Column(name = "step_id")
    @Comment("关联步骤ID（可空，针对某一步踩坑的反馈）")
    private Long stepId;

    @Column(name = "user_id", nullable = false)
    @Comment("评论用户ID")
    private Long userId;

    @Column(name = "content", nullable = false, length = 500)
    @Comment("评论内容")
    private String content;

    @Column(name = "parent_id")
    @Comment("父评论ID（可空，支持楼中楼回复）")
    private Long parentId;

    @Column(name = "like_count", nullable = false)
    @Comment("点赞数")
    private Integer likeCount;

    @Column(name = "status", nullable = false)
    @Comment("状态（0已删除 1正常）")
    private Integer status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充创建时间与默认值。
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.targetType == null) {
            this.targetType = 1;
        }
        if (this.likeCount == null) {
            this.likeCount = 0;
        }
        if (this.status == null) {
            this.status = 1;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Long getStepId() { return stepId; }
    public void setStepId(Long stepId) { this.stepId = stepId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

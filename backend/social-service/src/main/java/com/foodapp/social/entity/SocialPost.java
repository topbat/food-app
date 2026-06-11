package com.foodapp.social.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 社区帖子实体，对应表 social_post（作品晒图/打卡/美食日记）。
 */
@Entity
@Table(name = "social_post", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_recipe_id", columnList = "recipe_id")
})
@Comment("社区帖子表（作品晒图/打卡/美食日记）")
public class SocialPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("发布用户ID")
    private Long userId;

    @Column(name = "recipe_id")
    @Comment("关联菜谱ID（晒装盘时关联）")
    private Long recipeId;

    @Column(name = "content", length = 1000)
    @Comment("帖子文字内容")
    private String content;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    @Comment("图片URL列表（JSON数组字符串）")
    private String imageUrls;

    @Column(name = "post_type", nullable = false)
    @Comment("帖子类型（1作品晒图 2烹饪打卡 3美食日记）")
    private Integer postType;

    @Column(name = "like_count", nullable = false)
    @Comment("点赞数")
    private Integer likeCount;

    @Column(name = "comment_count", nullable = false)
    @Comment("评论数")
    private Integer commentCount;

    @Column(name = "status", nullable = false)
    @Comment("状态（0已删除 1正常）")
    private Integer status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Comment("更新时间")
    private LocalDateTime updatedAt;

    /**
     * 持久化前自动填充创建/更新时间与默认值。
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.postType == null) {
            this.postType = 1;
        }
        if (this.likeCount == null) {
            this.likeCount = 0;
        }
        if (this.commentCount == null) {
            this.commentCount = 0;
        }
        if (this.status == null) {
            this.status = 1;
        }
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
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    public Integer getPostType() { return postType; }
    public void setPostType(Integer postType) { this.postType = postType; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

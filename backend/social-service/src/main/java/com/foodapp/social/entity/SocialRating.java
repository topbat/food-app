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
 * 菜谱评分实体，对应表 social_rating（同一用户对同一菜谱仅一条记录，重复评分更新原记录）。
 */
@Entity
@Table(name = "social_rating", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_recipe", columnNames = {"user_id", "recipe_id"})
})
@Comment("菜谱评分表")
public class SocialRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("评分用户ID")
    private Long userId;

    @Column(name = "recipe_id", nullable = false)
    @Comment("关联菜谱ID")
    private Long recipeId;

    @Column(name = "score", nullable = false)
    @Comment("评分（1-5星）")
    private Integer score;

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
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

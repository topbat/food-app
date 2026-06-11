package com.foodapp.recipe.entity;

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
 * 菜谱主表实体（对应表 recipe_info）。
 * 营养数据按单人份计算；status=1 上架的菜谱才会出现在列表接口中。
 */
@Entity
@Table(name = "recipe_info")
@Comment("菜谱主表")
public class RecipeInfo {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    /** 菜谱名称（如：宫保鸡丁） */
    @Column(name = "title", nullable = false, length = 100)
    @Comment("菜谱名称（如：宫保鸡丁）")
    private String title;

    /** 封面图/视频URL */
    @Column(name = "cover_url", length = 255)
    @Comment("封面图/视频URL")
    private String coverUrl;

    /** 菜系（1川 2鲁 3粤 4苏 5闽 6浙 7湘 8徽 9家常） */
    @Column(name = "cuisine_type", nullable = false)
    @Comment("菜系（1川 2鲁 3粤 4苏 5闽 6浙 7湘 8徽 9家常）")
    private Integer cuisineType = 9;

    /** 难度（1入门 2进阶 3大厨） */
    @Column(name = "difficulty", nullable = false)
    @Comment("难度（1入门 2进阶 3大厨）")
    private Integer difficulty = 1;

    /** 总耗时（分钟） */
    @Column(name = "total_time_min", nullable = false)
    @Comment("总耗时（分钟）")
    private Integer totalTimeMin = 0;

    /** 份数（营养数据按单人份计算） */
    @Column(name = "servings", nullable = false)
    @Comment("份数（营养数据按单人份计算）")
    private Integer servings = 1;

    /** 单人份热量（kcal，由食材用量动态计算） */
    @Column(name = "calories_kcal", nullable = false, precision = 7, scale = 2)
    @Comment("单人份热量（kcal，由食材用量动态计算）")
    private BigDecimal caloriesKcal = BigDecimal.ZERO;

    /** 单人份碳水化合物（克） */
    @Column(name = "carbs_g", nullable = false, precision = 6, scale = 2)
    @Comment("单人份碳水化合物（克）")
    private BigDecimal carbsG = BigDecimal.ZERO;

    /** 单人份蛋白质（克） */
    @Column(name = "protein_g", nullable = false, precision = 6, scale = 2)
    @Comment("单人份蛋白质（克）")
    private BigDecimal proteinG = BigDecimal.ZERO;

    /** 单人份脂肪（克） */
    @Column(name = "fat_g", nullable = false, precision = 6, scale = 2)
    @Comment("单人份脂肪（克）")
    private BigDecimal fatG = BigDecimal.ZERO;

    /** 菜谱简介 */
    @Column(name = "description", length = 500)
    @Comment("菜谱简介")
    private String description;

    /** 小贴士（如：辣椒去籽防辣手） */
    @Column(name = "tips", length = 500)
    @Comment("小贴士（如：辣椒去籽防辣手）")
    private String tips;

    /** 状态（0下架 1上架 2待审核） */
    @Column(name = "status", nullable = false)
    @Comment("状态（0下架 1上架 2待审核）")
    private Integer status = 1;

    /** 浏览次数 */
    @Column(name = "view_count", nullable = false)
    @Comment("浏览次数")
    private Integer viewCount = 0;

    /** 点赞数 */
    @Column(name = "like_count", nullable = false)
    @Comment("点赞数")
    private Integer likeCount = 0;

    /** 作者用户ID（UGC菜谱） */
    @Column(name = "author_id")
    @Comment("作者用户ID（UGC菜谱）")
    private Long authorId;

    /** 来源（1官方 2用户UGC） */
    @Column(name = "source_type", nullable = false)
    @Comment("来源（1官方 2用户UGC）")
    private Integer sourceType = 1;

    /** 创建时间 */
    @Column(name = "created_at", nullable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column(name = "updated_at", nullable = false)
    @Comment("更新时间")
    private LocalDateTime updatedAt;

    /**
     * 持久化前自动填充创建/更新时间。
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    /**
     * 更新前自动刷新更新时间。
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getCuisineType() { return cuisineType; }
    public void setCuisineType(Integer cuisineType) { this.cuisineType = cuisineType; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public Integer getTotalTimeMin() { return totalTimeMin; }
    public void setTotalTimeMin(Integer totalTimeMin) { this.totalTimeMin = totalTimeMin; }
    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }
    public BigDecimal getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(BigDecimal caloriesKcal) { this.caloriesKcal = caloriesKcal; }
    public BigDecimal getCarbsG() { return carbsG; }
    public void setCarbsG(BigDecimal carbsG) { this.carbsG = carbsG; }
    public BigDecimal getProteinG() { return proteinG; }
    public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }
    public BigDecimal getFatG() { return fatG; }
    public void setFatG(BigDecimal fatG) { this.fatG = fatG; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

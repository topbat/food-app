package com.foodapp.recipe.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜谱摘要 VO（列表项与详情 info 共用，字段与接口契约一致）。
 */
public class RecipeSummaryVO {

    /** 菜谱ID */
    private Long id;
    /** 菜谱名称 */
    private String title;
    /** 封面图URL */
    private String coverUrl;
    /** 菜系编码（1川 2鲁 3粤 4苏 5闽 6浙 7湘 8徽 9家常） */
    private Integer cuisineType;
    /** 菜系中文名 */
    private String cuisineName;
    /** 难度（1入门 2进阶 3大厨） */
    private Integer difficulty;
    /** 总耗时（分钟） */
    private Integer totalTimeMin;
    /** 单人份热量（kcal） */
    private BigDecimal caloriesKcal;
    /** 单人份碳水（克） */
    private BigDecimal carbsG;
    /** 单人份蛋白质（克） */
    private BigDecimal proteinG;
    /** 单人份脂肪（克） */
    private BigDecimal fatG;
    /** 菜谱简介 */
    private String description;
    /** 浏览次数 */
    private Integer viewCount;
    /** 点赞数 */
    private Integer likeCount;
    /** 适宜标签名列表 */
    private List<String> tags;
    /** 小贴士（详情时填充） */
    private String tips;
    /** 份数（详情时填充） */
    private Integer servings;
    /** 来源（1官方 2用户UGC，详情时填充） */
    private Integer sourceType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getCuisineType() { return cuisineType; }
    public void setCuisineType(Integer cuisineType) { this.cuisineType = cuisineType; }
    public String getCuisineName() { return cuisineName; }
    public void setCuisineName(String cuisineName) { this.cuisineName = cuisineName; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public Integer getTotalTimeMin() { return totalTimeMin; }
    public void setTotalTimeMin(Integer totalTimeMin) { this.totalTimeMin = totalTimeMin; }
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
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }
    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
}

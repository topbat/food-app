package com.foodapp.recipe.entity;

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
 * 标签字典表实体（对应表 recipe_tag）。
 * 标签类型：1人群（减脂期/素食者等） 2功效（高蛋白/低卡等） 3场景（快手菜/熬夜党等）。
 */
@Entity
@Table(name = "recipe_tag", uniqueConstraints = @UniqueConstraint(name = "uk_tag_name", columnNames = "tag_name"))
@Comment("标签字典表")
public class RecipeTag {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    /** 标签名称（如：减脂期、素食、快手菜、熬夜党） */
    @Column(name = "tag_name", nullable = false, length = 30)
    @Comment("标签名称（如：减脂期、素食、快手菜、熬夜党）")
    private String tagName;

    /** 标签类型（1人群 2功效 3场景） */
    @Column(name = "tag_type", nullable = false)
    @Comment("标签类型（1人群 2功效 3场景）")
    private Integer tagType = 1;

    /** 创建时间 */
    @Column(name = "created_at", nullable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充创建时间。
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public Integer getTagType() { return tagType; }
    public void setTagType(Integer tagType) { this.tagType = tagType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

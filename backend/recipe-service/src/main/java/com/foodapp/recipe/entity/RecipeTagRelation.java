package com.foodapp.recipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

/**
 * 菜谱标签关联表实体（对应表 recipe_tag_relation）。
 * relation_type=1 适宜人群/功效/场景；relation_type=2 慎用/禁忌人群。
 */
@Entity
@Table(name = "recipe_tag_relation")
@Comment("菜谱标签关联表（含适宜与慎用区分）")
public class RecipeTagRelation {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    /** 关联菜谱ID */
    @Column(name = "recipe_id", nullable = false)
    @Comment("关联菜谱ID")
    private Long recipeId;

    /** 关联标签ID */
    @Column(name = "tag_id", nullable = false)
    @Comment("关联标签ID")
    private Long tagId;

    /** 关系类型（1适宜 2慎用/禁忌） */
    @Column(name = "relation_type", nullable = false)
    @Comment("关系类型（1适宜 2慎用/禁忌）")
    private Integer relationType = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public Integer getRelationType() { return relationType; }
    public void setRelationType(Integer relationType) { this.relationType = relationType; }
}

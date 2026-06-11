package com.foodapp.recipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 智能食材替换规则表实体（对应表 recipe_substitute_rule）。
 * 按「原食材 + 场景（减脂/控糖/素食/低嘌呤）」匹配替换目标食材。
 */
@Entity
@Table(name = "recipe_substitute_rule")
@Comment("智能食材替换规则表")
public class RecipeSubstituteRule {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    /** 原食材ID（如：五花肉） */
    @Column(name = "source_ingredient_id", nullable = false)
    @Comment("原食材ID（如：五花肉）")
    private Long sourceIngredientId;

    /** 替换为食材ID（如：鸡胸肉） */
    @Column(name = "target_ingredient_id", nullable = false)
    @Comment("替换为食材ID（如：鸡胸肉）")
    private Long targetIngredientId;

    /** 适用场景（减脂/控糖/素食/低嘌呤） */
    @Column(name = "scene", nullable = false, length = 20)
    @Comment("适用场景（减脂/控糖/素食/低嘌呤）")
    private String scene;

    /** 替换理由说明 */
    @Column(name = "reason", length = 200)
    @Comment("替换理由说明")
    private String reason;

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
    public Long getSourceIngredientId() { return sourceIngredientId; }
    public void setSourceIngredientId(Long sourceIngredientId) { this.sourceIngredientId = sourceIngredientId; }
    public Long getTargetIngredientId() { return targetIngredientId; }
    public void setTargetIngredientId(Long targetIngredientId) { this.targetIngredientId = targetIngredientId; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

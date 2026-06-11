package com.foodapp.user.entity;

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
 * 用户标签实体（Z世代画像标签），对应表 user_tag。
 */
@Entity
@Table(name = "user_tag")
@Comment("用户标签表（Z世代画像标签）")
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("关联用户ID")
    private Long userId;

    @Column(name = "tag_name", nullable = false, length = 30)
    @Comment("标签名称（如：熬夜党、健身狂、生理期）")
    private String tagName;

    @Column(name = "tag_type", nullable = false)
    @Comment("标签类型（1人群标签 2状态标签）")
    private Integer tagType;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充创建时间与默认标签类型。
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.tagType == null) {
            this.tagType = 1;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public Integer getTagType() { return tagType; }
    public void setTagType(Integer tagType) { this.tagType = tagType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

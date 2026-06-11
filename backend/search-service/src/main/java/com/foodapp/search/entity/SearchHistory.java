package com.foodapp.search.entity;

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
 * 搜索历史实体，对应表 search_history（与 sql/04_search.sql 一致）。
 */
@Entity
@Table(name = "search_history", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Comment("搜索历史表")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id")
    @Comment("关联用户ID（游客为空）")
    private Long userId;

    @Column(name = "keyword", nullable = false, length = 100)
    @Comment("搜索关键词")
    private String keyword;

    @Column(name = "search_type", nullable = false)
    @Comment("搜索类型（1关键词 2按食材/冰箱清理 3按营养目标 4按人群）")
    private Integer searchType;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充创建时间与默认搜索类型。
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.searchType == null) {
            this.searchType = 1;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getSearchType() { return searchType; }
    public void setSearchType(Integer searchType) { this.searchType = searchType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

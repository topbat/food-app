package com.foodapp.search.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 热搜关键词实体，对应表 search_hot_keyword（与 sql/04_search.sql 一致）。
 * 关键词唯一，重复搜索时累加 search_count，用于热度排序。
 */
@Entity
@Table(name = "search_hot_keyword", uniqueConstraints = {
        @UniqueConstraint(name = "uk_keyword", columnNames = "keyword")
})
@Comment("热搜关键词表")
public class SearchHotKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "keyword", nullable = false, length = 100)
    @Comment("热搜关键词")
    private String keyword;

    @Column(name = "search_count", nullable = false)
    @Comment("累计搜索次数（用于热度排序）")
    private Long searchCount;

    @Column(name = "updated_at", nullable = false)
    @Comment("更新时间")
    private LocalDateTime updatedAt;

    /**
     * 持久化前自动填充更新时间与默认计数。
     */
    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
        if (this.searchCount == null) {
            this.searchCount = 0L;
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
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getSearchCount() { return searchCount; }
    public void setSearchCount(Long searchCount) { this.searchCount = searchCount; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

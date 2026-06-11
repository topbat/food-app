package com.foodapp.search.vo;

import java.time.LocalDateTime;

/**
 * 搜索历史项视图对象（契约：{id,keyword,searchType,createdAt}，不暴露 userId）。
 */
public class SearchHistoryVO {

    /** 历史记录ID */
    private Long id;
    /** 搜索关键词 */
    private String keyword;
    /** 搜索类型（1关键词 2按食材 3按营养目标 4按人群） */
    private Integer searchType;
    /** 搜索时间 */
    private LocalDateTime createdAt;

    public SearchHistoryVO() {
    }

    public SearchHistoryVO(Long id, String keyword, Integer searchType, LocalDateTime createdAt) {
        this.id = id;
        this.keyword = keyword;
        this.searchType = searchType;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getSearchType() { return searchType; }
    public void setSearchType(Integer searchType) { this.searchType = searchType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

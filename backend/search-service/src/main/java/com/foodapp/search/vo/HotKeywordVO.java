package com.foodapp.search.vo;

/**
 * 热搜榜单项视图对象（契约：{keyword,searchCount}）。
 */
public class HotKeywordVO {

    /** 热搜关键词 */
    private String keyword;
    /** 累计搜索次数 */
    private Long searchCount;

    public HotKeywordVO() {
    }

    public HotKeywordVO(String keyword, Long searchCount) {
        this.keyword = keyword;
        this.searchCount = searchCount;
    }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getSearchCount() { return searchCount; }
    public void setSearchCount(Long searchCount) { this.searchCount = searchCount; }
}

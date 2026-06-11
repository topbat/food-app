package com.foodapp.user.vo;

/**
 * 用户标签展示VO。
 */
public class TagVO {

    /** 标签ID */
    private Long id;
    /** 标签名称 */
    private String tagName;
    /** 标签类型（1人群标签 2状态标签） */
    private Integer tagType;

    public TagVO() {
    }

    public TagVO(Long id, String tagName, Integer tagType) {
        this.id = id;
        this.tagName = tagName;
        this.tagType = tagType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public Integer getTagType() { return tagType; }
    public void setTagType(Integer tagType) { this.tagType = tagType; }
}

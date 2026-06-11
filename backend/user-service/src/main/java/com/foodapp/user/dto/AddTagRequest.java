package com.foodapp.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 添加标签请求入参。
 */
public class AddTagRequest {

    /** 标签名称（如：熬夜党、健身狂、生理期） */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 30, message = "标签名称长度不能超过30个字符")
    private String tagName;

    /** 标签类型（1人群标签 2状态标签） */
    @NotNull(message = "标签类型不能为空")
    @Min(value = 1, message = "标签类型只能为1（人群）或2（状态）")
    @Max(value = 2, message = "标签类型只能为1（人群）或2（状态）")
    private Integer tagType;

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public Integer getTagType() { return tagType; }
    public void setTagType(Integer tagType) { this.tagType = tagType; }
}

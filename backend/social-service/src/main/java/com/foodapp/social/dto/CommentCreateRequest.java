package com.foodapp.social.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 发评论请求体。
 */
public class CommentCreateRequest {

    /** 评论对象类型（1菜谱 2社区帖子） */
    @NotNull(message = "评论对象类型不能为空")
    @Min(value = 1, message = "评论对象类型仅支持1菜谱/2帖子")
    @Max(value = 2, message = "评论对象类型仅支持1菜谱/2帖子")
    private Integer targetType;

    /** 评论对象ID（菜谱ID或帖子ID） */
    @NotNull(message = "评论对象ID不能为空")
    private Long targetId;

    /** 关联步骤ID（可空，针对某一步踩坑反馈） */
    private Long stepId;

    /** 评论内容 */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容最长500字")
    private String content;

    /** 父评论ID（可空，楼中楼回复） */
    private Long parentId;

    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Long getStepId() { return stepId; }
    public void setStepId(Long stepId) { this.stepId = stepId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}

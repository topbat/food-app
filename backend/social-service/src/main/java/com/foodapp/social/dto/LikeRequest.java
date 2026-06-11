package com.foodapp.social.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 点赞 toggle 请求体。
 */
public class LikeRequest {

    /** 点赞对象类型（1帖子 2评论 3菜谱） */
    @NotNull(message = "点赞对象类型不能为空")
    @Min(value = 1, message = "点赞对象类型仅支持1帖子/2评论/3菜谱")
    @Max(value = 3, message = "点赞对象类型仅支持1帖子/2评论/3菜谱")
    private Integer targetType;

    /** 点赞对象ID */
    @NotNull(message = "点赞对象ID不能为空")
    private Long targetId;

    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
}

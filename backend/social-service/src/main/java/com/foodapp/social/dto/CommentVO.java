package com.foodapp.social.dto;

import java.time.LocalDateTime;

/**
 * 评论列表项 VO（已装配昵称头像）。
 */
public class CommentVO {

    /** 评论ID */
    private Long id;
    /** 评论用户ID */
    private Long userId;
    /** 评论用户昵称（调用户服务装配） */
    private String nickname;
    /** 评论用户头像（调用户服务装配） */
    private String avatarUrl;
    /** 关联步骤ID（可空） */
    private Long stepId;
    /** 评论内容 */
    private String content;
    /** 父评论ID（可空） */
    private Long parentId;
    /** 点赞数 */
    private Integer likeCount;
    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Long getStepId() { return stepId; }
    public void setStepId(Long stepId) { this.stepId = stepId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

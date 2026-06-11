package com.foodapp.social.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表项 VO（已装配昵称头像，imageUrls 已由 JSON 字符串还原为列表）。
 */
public class PostVO {

    /** 帖子ID */
    private Long id;
    /** 发布用户ID */
    private Long userId;
    /** 发布用户昵称（调用户服务装配） */
    private String nickname;
    /** 发布用户头像（调用户服务装配） */
    private String avatarUrl;
    /** 关联菜谱ID（可空） */
    private Long recipeId;
    /** 帖子文字内容 */
    private String content;
    /** 图片URL列表 */
    private List<String> imageUrls;
    /** 帖子类型（1作品晒图 2烹饪打卡 3美食日记） */
    private Integer postType;
    /** 点赞数 */
    private Integer likeCount;
    /** 评论数 */
    private Integer commentCount;
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
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public Integer getPostType() { return postType; }
    public void setPostType(Integer postType) { this.postType = postType; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

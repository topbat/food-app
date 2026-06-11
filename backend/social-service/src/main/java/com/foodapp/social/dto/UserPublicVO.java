package com.foodapp.social.dto;

/**
 * 用户公开信息 VO（来自用户服务 GET /api/user/public/{id}）。
 */
public class UserPublicVO {

    /** 用户ID */
    private Long id;
    /** 昵称 */
    private String nickname;
    /** 头像URL */
    private String avatarUrl;

    public UserPublicVO() {
    }

    public UserPublicVO(Long id, String nickname, String avatarUrl) {
        this.id = id;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

package com.foodapp.user.vo;

/**
 * 用户公开信息（供社交服务/前端展示，无需登录即可访问）。
 */
public class PublicUserVO {

    /** 用户ID */
    private Long id;
    /** 昵称 */
    private String nickname;
    /** 头像URL */
    private String avatarUrl;

    public PublicUserVO() {
    }

    public PublicUserVO(Long id, String nickname, String avatarUrl) {
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

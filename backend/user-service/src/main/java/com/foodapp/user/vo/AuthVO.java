package com.foodapp.user.vo;

/**
 * 注册/登录成功响应：token + 用户基础信息。
 * 用 VO 隔离实体，保证密码等敏感字段绝不出现在响应JSON中。
 */
public class AuthVO {

    /** JWT 令牌 */
    private String token;

    /** 用户基础信息 */
    private SimpleUserVO user;

    public AuthVO() {
    }

    public AuthVO(String token, SimpleUserVO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public SimpleUserVO getUser() { return user; }
    public void setUser(SimpleUserVO user) { this.user = user; }

    /**
     * 用户基础信息（注册/登录响应用）。
     */
    public static class SimpleUserVO {

        /** 用户ID */
        private Long id;
        /** 登录用户名 */
        private String username;
        /** 昵称 */
        private String nickname;
        /** 头像URL */
        private String avatarUrl;

        public SimpleUserVO() {
        }

        public SimpleUserVO(Long id, String username, String nickname, String avatarUrl) {
            this.id = id;
            this.username = username;
            this.nickname = nickname;
            this.avatarUrl = avatarUrl;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
}

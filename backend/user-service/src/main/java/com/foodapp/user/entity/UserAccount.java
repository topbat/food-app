package com.foodapp.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户账号实体，对应表 user_account。
 */
@Entity
@Table(name = "user_account")
@Comment("用户账号表")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    @Comment("登录用户名（唯一）")
    private String username;

    /** 密码绝不允许出现在任何响应JSON中，序列化时忽略 */
    @JsonIgnore
    @Column(name = "password", nullable = false, length = 100)
    @Comment("密码（BCrypt加密存储，禁止明文）")
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    @Comment("昵称")
    private String nickname;

    @Column(name = "avatar_url", length = 255)
    @Comment("头像URL")
    private String avatarUrl;

    @Column(name = "gender")
    @Comment("性别（0未知 1男 2女）")
    private Integer gender;

    @Column(name = "birth_date")
    @Comment("出生日期")
    private LocalDate birthDate;

    @Column(name = "status", nullable = false)
    @Comment("账号状态（0禁用 1正常）")
    private Integer status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Comment("更新时间")
    private LocalDateTime updatedAt;

    /**
     * 持久化前自动填充创建/更新时间与默认值。
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.gender == null) {
            this.gender = 0;
        }
        if (this.status == null) {
            this.status = 1;
        }
    }

    /**
     * 更新前自动刷新更新时间。
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

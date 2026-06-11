package com.foodapp.social.dto;

import java.time.LocalDateTime;

/**
 * 徽章墙 VO：全部徽章 + 当前用户是否已获得。
 */
public class BadgeVO {

    /** 徽章ID */
    private Long id;
    /** 徽章名称 */
    private String badgeName;
    /** 徽章描述与获得条件说明 */
    private String badgeDesc;
    /** 徽章图标（emoji） */
    private String icon;
    /** 当前用户是否已获得 */
    private boolean obtained;
    /** 获得时间（未获得为 null） */
    private LocalDateTime obtainedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }
    public String getBadgeDesc() { return badgeDesc; }
    public void setBadgeDesc(String badgeDesc) { this.badgeDesc = badgeDesc; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isObtained() { return obtained; }
    public void setObtained(boolean obtained) { this.obtained = obtained; }
    public LocalDateTime getObtainedAt() { return obtainedAt; }
    public void setObtainedAt(LocalDateTime obtainedAt) { this.obtainedAt = obtainedAt; }
}

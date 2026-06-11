package com.foodapp.social.dto;

/**
 * 新获得徽章简要 VO（打卡/发帖后返回的 newBadges 列表项）。
 */
public class BadgeBriefVO {

    /** 徽章名称 */
    private String badgeName;
    /** 徽章图标（emoji） */
    private String icon;

    public BadgeBriefVO() {
    }

    public BadgeBriefVO(String badgeName, String icon) {
        this.badgeName = badgeName;
        this.icon = icon;
    }

    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}

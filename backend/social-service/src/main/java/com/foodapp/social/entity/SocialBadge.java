package com.foodapp.social.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

/**
 * 成就徽章字典实体，对应表 social_badge（dev 环境由 data.sql 灌入种子数据）。
 */
@Entity
@Table(name = "social_badge")
@Comment("成就徽章字典表")
public class SocialBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "badge_name", nullable = false, length = 50)
    @Comment("徽章名称（如：川菜小当家）")
    private String badgeName;

    @Column(name = "badge_desc", length = 200)
    @Comment("徽章描述与获得条件说明")
    private String badgeDesc;

    @Column(name = "icon", length = 10)
    @Comment("徽章图标（emoji或图片URL）")
    private String icon;

    @Column(name = "condition_type", nullable = false, length = 30)
    @Comment("达成条件类型（CHECKIN_DAYS连续打卡/POST_COUNT发帖数/COOK_COUNT烹饪次数）")
    private String conditionType;

    @Column(name = "condition_value", nullable = false)
    @Comment("达成条件数值（如连续打卡7天）")
    private Integer conditionValue;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }
    public String getBadgeDesc() { return badgeDesc; }
    public void setBadgeDesc(String badgeDesc) { this.badgeDesc = badgeDesc; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public Integer getConditionValue() { return conditionValue; }
    public void setConditionValue(Integer conditionValue) { this.conditionValue = conditionValue; }
}

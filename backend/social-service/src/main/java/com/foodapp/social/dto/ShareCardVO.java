package com.foodapp.social.dto;

/**
 * 分享卡数据 VO（前端用于渲染分享卡片）。
 */
public class ShareCardVO {

    /** 主标题文案 */
    private String title;
    /** 副标题文案 */
    private String subtitle;
    /** 热量文案（如"本餐热量约350千卡"，未传热量时为 null） */
    private String calorieText;
    /** 运动等效文案（如"相当于慢跑30分钟"=热量/11.6 分钟取整，未传热量时为 null） */
    private String equivalentText;
    /** 当前连续打卡天数 */
    private int continuousDays;
    /** 用户昵称（调用户服务获取） */
    private String nickname;
    /** 当天日期字符串（yyyy-MM-dd） */
    private String date;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getCalorieText() { return calorieText; }
    public void setCalorieText(String calorieText) { this.calorieText = calorieText; }
    public String getEquivalentText() { return equivalentText; }
    public void setEquivalentText(String equivalentText) { this.equivalentText = equivalentText; }
    public int getContinuousDays() { return continuousDays; }
    public void setContinuousDays(int continuousDays) { this.continuousDays = continuousDays; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}

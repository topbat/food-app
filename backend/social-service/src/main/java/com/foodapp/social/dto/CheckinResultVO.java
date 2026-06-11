package com.foodapp.social.dto;

import java.util.List;

/**
 * 打卡结果 VO：当前连续打卡天数 + 本次新达成的徽章列表。
 */
public class CheckinResultVO {

    /** 当前连续打卡天数 */
    private int continuousDays;
    /** 本次打卡新达成的徽章（CHECKIN_DAYS 类） */
    private List<BadgeBriefVO> newBadges;

    public CheckinResultVO() {
    }

    public CheckinResultVO(int continuousDays, List<BadgeBriefVO> newBadges) {
        this.continuousDays = continuousDays;
        this.newBadges = newBadges;
    }

    public int getContinuousDays() { return continuousDays; }
    public void setContinuousDays(int continuousDays) { this.continuousDays = continuousDays; }
    public List<BadgeBriefVO> getNewBadges() { return newBadges; }
    public void setNewBadges(List<BadgeBriefVO> newBadges) { this.newBadges = newBadges; }
}

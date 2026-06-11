package com.foodapp.social.dto;

import java.util.List;

/**
 * 打卡月历 VO：某月已打卡日期列表 + 当前连续打卡天数。
 */
public class CheckinMonthVO {

    /** 该月已打卡日期（yyyy-MM-dd 字符串列表） */
    private List<String> dates;
    /** 当前连续打卡天数 */
    private int continuousDays;

    public CheckinMonthVO() {
    }

    public CheckinMonthVO(List<String> dates, int continuousDays) {
        this.dates = dates;
        this.continuousDays = continuousDays;
    }

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }
    public int getContinuousDays() { return continuousDays; }
    public void setContinuousDays(int continuousDays) { this.continuousDays = continuousDays; }
}

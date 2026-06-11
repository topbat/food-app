package com.foodapp.user.vo;

import java.math.BigDecimal;

/**
 * 月度日历单日汇总VO：日期 + 当日总热量。
 */
public class MonthDailyVO {

    /** 日期（格式 yyyy-MM-dd） */
    private String date;
    /** 当日总热量（kcal） */
    private BigDecimal totalCalories;

    public MonthDailyVO() {
    }

    public MonthDailyVO(String date, BigDecimal totalCalories) {
        this.date = date;
        this.totalCalories = totalCalories;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public BigDecimal getTotalCalories() { return totalCalories; }
    public void setTotalCalories(BigDecimal totalCalories) { this.totalCalories = totalCalories; }
}

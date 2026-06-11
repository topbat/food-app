package com.foodapp.social.dto;

/**
 * 菜谱评分统计 VO。
 */
public class RatingVO {

    /** 平均评分（保留1位小数，无评分时为0.0） */
    private double avgScore;
    /** 评分人数 */
    private long ratingCount;

    public RatingVO() {
    }

    public RatingVO(double avgScore, long ratingCount) {
        this.avgScore = avgScore;
        this.ratingCount = ratingCount;
    }

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
    public long getRatingCount() { return ratingCount; }
    public void setRatingCount(long ratingCount) { this.ratingCount = ratingCount; }
}

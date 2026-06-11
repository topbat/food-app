package com.foodapp.social.dto;

/**
 * 点赞 toggle 结果 VO。
 */
public class LikeVO {

    /** 本次操作后是否处于已点赞状态 */
    private boolean liked;
    /** 该对象当前点赞总数 */
    private long likeCount;

    public LikeVO() {
    }

    public LikeVO(boolean liked, long likeCount) {
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
}

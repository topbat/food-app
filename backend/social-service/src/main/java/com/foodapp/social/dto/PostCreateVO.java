package com.foodapp.social.dto;

import java.util.List;

/**
 * 发帖结果 VO：新帖子ID + 本次发帖新达成的徽章列表。
 */
public class PostCreateVO {

    /** 新帖子ID */
    private Long id;
    /** 本次发帖新达成的徽章（POST_COUNT 类） */
    private List<BadgeBriefVO> newBadges;

    public PostCreateVO() {
    }

    public PostCreateVO(Long id, List<BadgeBriefVO> newBadges) {
        this.id = id;
        this.newBadges = newBadges;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public List<BadgeBriefVO> getNewBadges() { return newBadges; }
    public void setNewBadges(List<BadgeBriefVO> newBadges) { this.newBadges = newBadges; }
}

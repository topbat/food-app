package com.foodapp.social.repository;

import com.foodapp.social.entity.SocialPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 社区帖子仓储。
 */
public interface SocialPostRepository extends JpaRepository<SocialPost, Long> {

    /**
     * 分页查询正常状态帖子（作品墙，不筛类型）。
     *
     * @param status   状态（1正常）
     * @param pageable 分页参数
     * @return 帖子分页
     */
    Page<SocialPost> findByStatus(Integer status, Pageable pageable);

    /**
     * 分页查询指定类型的正常状态帖子。
     *
     * @param postType 帖子类型（1作品晒图 2烹饪打卡 3美食日记）
     * @param status   状态（1正常）
     * @param pageable 分页参数
     * @return 帖子分页
     */
    Page<SocialPost> findByPostTypeAndStatus(Integer postType, Integer status, Pageable pageable);

    /**
     * 统计用户累计发帖数（用于 POST_COUNT 类徽章达成判断）。
     *
     * @param userId 用户ID
     * @param status 状态（1正常）
     * @return 发帖数
     */
    long countByUserIdAndStatus(Long userId, Integer status);
}

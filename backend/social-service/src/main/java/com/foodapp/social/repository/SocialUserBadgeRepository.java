package com.foodapp.social.repository;

import com.foodapp.social.entity.SocialUserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 用户已获徽章仓储。
 */
public interface SocialUserBadgeRepository extends JpaRepository<SocialUserBadge, Long> {

    /**
     * 判断用户是否已获得某徽章（防止重复发放）。
     *
     * @param userId  用户ID
     * @param badgeId 徽章ID
     * @return true 已获得
     */
    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    /**
     * 查询用户已获得的全部徽章记录。
     *
     * @param userId 用户ID
     * @return 已获徽章记录列表
     */
    List<SocialUserBadge> findByUserId(Long userId);
}

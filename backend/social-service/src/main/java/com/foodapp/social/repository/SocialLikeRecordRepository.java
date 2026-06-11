package com.foodapp.social.repository;

import com.foodapp.social.entity.SocialLikeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 点赞记录仓储。
 */
public interface SocialLikeRecordRepository extends JpaRepository<SocialLikeRecord, Long> {

    /**
     * 查询用户对某对象的点赞记录（toggle 判断依据）。
     *
     * @param userId     用户ID
     * @param targetType 点赞对象类型（1帖子 2评论 3菜谱）
     * @param targetId   点赞对象ID
     * @return 点赞记录（可能不存在）
     */
    Optional<SocialLikeRecord> findByUserIdAndTargetTypeAndTargetId(Long userId, Integer targetType, Long targetId);

    /**
     * 统计某对象的点赞总数（菜谱类点赞无外表计数，直接按记录统计）。
     *
     * @param targetType 点赞对象类型
     * @param targetId   点赞对象ID
     * @return 点赞总数
     */
    long countByTargetTypeAndTargetId(Integer targetType, Long targetId);
}

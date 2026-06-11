package com.foodapp.social.repository;

import com.foodapp.social.entity.SocialBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 成就徽章字典仓储。
 */
public interface SocialBadgeRepository extends JpaRepository<SocialBadge, Long> {

    /**
     * 查询指定条件类型下、达成阈值不高于当前数值的全部徽章（候选达成徽章）。
     *
     * @param conditionType  条件类型（CHECKIN_DAYS / POST_COUNT / COOK_COUNT）
     * @param conditionValue 用户当前数值（连续打卡天数或发帖数）
     * @return 候选徽章列表
     */
    List<SocialBadge> findByConditionTypeAndConditionValueLessThanEqual(String conditionType, Integer conditionValue);
}

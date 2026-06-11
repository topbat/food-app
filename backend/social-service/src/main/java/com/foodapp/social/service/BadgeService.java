package com.foodapp.social.service;

import com.foodapp.social.dto.BadgeBriefVO;
import com.foodapp.social.dto.BadgeVO;
import com.foodapp.social.entity.SocialBadge;
import com.foodapp.social.entity.SocialUserBadge;
import com.foodapp.social.repository.SocialBadgeRepository;
import com.foodapp.social.repository.SocialUserBadgeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成就徽章服务。
 * 提供徽章墙查询（全部徽章 + 当前用户是否已获得），
 * 以及通用的徽章达成判断与发放（发帖 POST_COUNT / 打卡 CHECKIN_DAYS 共用）。
 */
@Service
public class BadgeService {

    private static final Logger log = LoggerFactory.getLogger(BadgeService.class);

    private final SocialBadgeRepository badgeRepository;
    private final SocialUserBadgeRepository userBadgeRepository;

    /**
     * 构造注入徽章字典与用户已获徽章仓储。
     *
     * @param badgeRepository     徽章字典仓储
     * @param userBadgeRepository 用户已获徽章仓储
     */
    public BadgeService(SocialBadgeRepository badgeRepository,
                        SocialUserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    /**
     * 查询徽章墙：全部徽章字典 + 当前用户是否已获得及获得时间。
     *
     * @param userId 当前登录用户ID
     * @return 徽章墙列表（按徽章ID升序）
     */
    public List<BadgeVO> myBadges(Long userId) {
        Map<Long, LocalDateTime> obtainedMap = userBadgeRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(SocialUserBadge::getBadgeId, SocialUserBadge::getObtainedAt));
        List<BadgeVO> result = new ArrayList<>();
        for (SocialBadge badge : badgeRepository.findAll()) {
            BadgeVO vo = new BadgeVO();
            vo.setId(badge.getId());
            vo.setBadgeName(badge.getBadgeName());
            vo.setBadgeDesc(badge.getBadgeDesc());
            vo.setIcon(badge.getIcon());
            LocalDateTime obtainedAt = obtainedMap.get(badge.getId());
            vo.setObtained(obtainedAt != null);
            vo.setObtainedAt(obtainedAt);
            result.add(vo);
        }
        return result;
    }

    /**
     * 通用徽章达成判断与发放：
     * 查询指定条件类型下达成阈值（condition_value &lt;= 当前数值）的全部徽章，
     * 对尚未获得的徽章插入 social_user_badge 记录并返回（同一徽章不会重复发放）。
     * 需在调用方业务事务内执行，保证业务记录与徽章发放同事务。
     *
     * @param userId        用户ID
     * @param conditionType 条件类型（POST_COUNT 发帖数 / CHECKIN_DAYS 连续打卡天数）
     * @param currentValue  用户当前数值（累计发帖数或当前连续打卡天数）
     * @return 本次新达成的徽章简要列表（无新达成时为空列表）
     */
    @Transactional
    public List<BadgeBriefVO> awardBadges(Long userId, String conditionType, int currentValue) {
        List<SocialBadge> candidates =
                badgeRepository.findByConditionTypeAndConditionValueLessThanEqual(conditionType, currentValue);
        List<BadgeBriefVO> newBadges = new ArrayList<>();
        for (SocialBadge badge : candidates) {
            // 关键判断：已获得过的徽章不重复发放
            if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
                continue;
            }
            SocialUserBadge userBadge = new SocialUserBadge();
            userBadge.setUserId(userId);
            userBadge.setBadgeId(badge.getId());
            userBadgeRepository.save(userBadge);
            log.info("用户{}达成徽章[{}]", userId, badge.getBadgeName());
            newBadges.add(new BadgeBriefVO(badge.getBadgeName(), badge.getIcon()));
        }
        return newBadges;
    }
}

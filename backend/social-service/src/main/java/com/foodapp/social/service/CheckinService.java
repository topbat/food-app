package com.foodapp.social.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.social.dto.BadgeBriefVO;
import com.foodapp.social.dto.CheckinMonthVO;
import com.foodapp.social.dto.CheckinRequest;
import com.foodapp.social.dto.CheckinResultVO;
import com.foodapp.social.entity.SocialCheckin;
import com.foodapp.social.repository.SocialCheckinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * 烹饪打卡服务。
 * 同日重复打卡拦截、连续打卡天数计算（昨日有记录则 +1，否则重置为1）、
 * 打卡后触发 CHECKIN_DAYS 类徽章、打卡月历查询。
 */
@Service
public class CheckinService {

    private static final Logger log = LoggerFactory.getLogger(CheckinService.class);

    /** 连续打卡类徽章条件类型 */
    private static final String CONDITION_CHECKIN_DAYS = "CHECKIN_DAYS";

    /** 打卡日期输出格式 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SocialCheckinRepository checkinRepository;
    private final BadgeService badgeService;

    /**
     * 构造注入打卡仓储与徽章服务。
     *
     * @param checkinRepository 打卡仓储
     * @param badgeService      徽章服务（打卡后判断 CHECKIN_DAYS 徽章）
     */
    public CheckinService(SocialCheckinRepository checkinRepository, BadgeService badgeService) {
        this.checkinRepository = checkinRepository;
        this.badgeService = badgeService;
    }

    /**
     * 打卡：同日重复打卡抛 40900 业务冲突；
     * 连续天数 = 昨日有打卡记录则其 continuous_days + 1，否则重置为 1；
     * 打卡记录与徽章发放同事务。
     *
     * @param userId  当前登录用户ID
     * @param request 打卡请求体（recipeId/note 均可空）
     * @return 当前连续打卡天数 + 本次新达成的徽章列表
     * @throws BusinessException 今日已打卡时抛出（40900）
     */
    @Transactional
    public CheckinResultVO checkin(Long userId, CheckinRequest request) {
        LocalDate today = LocalDate.now();
        // 关键判断：同一用户同一天仅可打卡一次
        if (checkinRepository.findByUserIdAndCheckinDate(userId, today).isPresent()) {
            log.warn("[打卡] 用户{}今日({})已打卡, 拦截重复打卡", userId, today);
            throw new BusinessException(ResultCode.BIZ_CONFLICT, "今日已打卡");
        }
        // 关键判断：昨日有打卡记录则连续天数 +1，否则从 1 重新计数
        Optional<SocialCheckin> yesterday = checkinRepository.findByUserIdAndCheckinDate(userId, today.minusDays(1));
        int continuousDays = yesterday.map(c -> c.getContinuousDays() + 1).orElse(1);

        SocialCheckin checkin = new SocialCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setRecipeId(request.getRecipeId());
        checkin.setNote(request.getNote());
        checkin.setContinuousDays(continuousDays);
        checkinRepository.save(checkin);
        log.info("[打卡] 用户{}打卡成功, 日期={}, 连续打卡{}天", userId, today, continuousDays);

        List<BadgeBriefVO> newBadges = badgeService.awardBadges(userId, CONDITION_CHECKIN_DAYS, continuousDays);
        return new CheckinResultVO(continuousDays, newBadges);
    }

    /**
     * 打卡月历：返回指定月份（yyyy-MM）已打卡日期列表 + 当前连续打卡天数。
     *
     * @param userId 当前登录用户ID
     * @param month  月份字符串（yyyy-MM，可空，默认当月）
     * @return 当月打卡日期列表 + 当前连续打卡天数
     * @throws BusinessException 月份格式非法时抛出（40000）
     */
    public CheckinMonthVO myCheckin(Long userId, String month) {
        YearMonth yearMonth;
        try {
            yearMonth = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "月份格式应为yyyy-MM");
        }
        List<String> dates = checkinRepository
                .findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth())
                .stream()
                .map(c -> c.getCheckinDate().format(DATE_FORMATTER))
                .toList();
        return new CheckinMonthVO(dates, currentContinuousDays(userId));
    }

    /**
     * 计算当前连续打卡天数：取最近一次打卡记录的 continuous_days；
     * 若最近一次打卡既不是今天也不是昨天（连续已中断），则返回 0。
     *
     * @param userId 用户ID
     * @return 当前连续打卡天数（从未打卡或已中断为 0）
     */
    public int currentContinuousDays(Long userId) {
        Optional<SocialCheckin> latest = checkinRepository.findTopByUserIdOrderByCheckinDateDesc(userId);
        if (latest.isEmpty()) {
            return 0;
        }
        LocalDate latestDate = latest.get().getCheckinDate();
        LocalDate today = LocalDate.now();
        // 关键判断：最近打卡是今天或昨天才算连续未中断，否则连续天数归零
        if (latestDate.equals(today) || latestDate.equals(today.minusDays(1))) {
            return latest.get().getContinuousDays();
        }
        log.info("[打卡] 用户{}连续打卡已中断, 最近打卡日={}, 连续天数归零", userId, latestDate);
        return 0;
    }
}

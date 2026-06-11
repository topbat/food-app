package com.foodapp.social.repository;

import com.foodapp.social.entity.SocialCheckin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 烹饪打卡仓储。
 */
public interface SocialCheckinRepository extends JpaRepository<SocialCheckin, Long> {

    /**
     * 查询某用户某天的打卡记录（用于同日重复打卡拦截与昨日连续天数计算）。
     *
     * @param userId      用户ID
     * @param checkinDate 打卡日期
     * @return 打卡记录（可能不存在）
     */
    Optional<SocialCheckin> findByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);

    /**
     * 查询某用户在日期区间内的打卡记录（用于打卡月历）。
     *
     * @param userId 用户ID
     * @param start  起始日期（含）
     * @param end    结束日期（含）
     * @return 打卡记录列表（按日期升序）
     */
    List<SocialCheckin> findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(Long userId, LocalDate start, LocalDate end);

    /**
     * 查询某用户最近一次打卡记录（用于计算当前连续打卡天数）。
     *
     * @param userId 用户ID
     * @return 最近一次打卡记录（可能不存在）
     */
    Optional<SocialCheckin> findTopByUserIdOrderByCheckinDateDesc(Long userId);
}

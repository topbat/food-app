package com.foodapp.user.repository;

import com.foodapp.user.entity.UserDietRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 饮食日历记录数据访问层。
 */
public interface UserDietRecordRepository extends JpaRepository<UserDietRecord, Long> {

    /**
     * 查询某用户某天的全部饮食记录（按餐次、ID排序）。
     *
     * @param userId     用户ID
     * @param recordDate 记录日期
     * @return 饮食记录列表
     */
    List<UserDietRecord> findByUserIdAndRecordDateOrderByMealTypeAscIdAsc(Long userId, LocalDate recordDate);

    /**
     * 按日聚合某用户一段时间内的总热量（月度日历使用）。
     *
     * @param userId 用户ID
     * @param start  起始日期（含）
     * @param end    结束日期（含）
     * @return 每行为 [记录日期, 当日总热量]
     */
    @Query("select r.recordDate, sum(r.caloriesKcal) from UserDietRecord r " +
            "where r.userId = :userId and r.recordDate between :start and :end " +
            "group by r.recordDate order by r.recordDate asc")
    List<Object[]> sumCaloriesGroupByDate(@Param("userId") Long userId,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end);
}

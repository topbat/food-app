package com.foodapp.user.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.user.dto.DietRecordRequest;
import com.foodapp.user.entity.UserDietRecord;
import com.foodapp.user.entity.UserHealthProfile;
import com.foodapp.user.repository.UserDietRecordRepository;
import com.foodapp.user.repository.UserHealthProfileRepository;
import com.foodapp.user.vo.DailyDietVO;
import com.foodapp.user.vo.MonthDailyVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 饮食日历业务层：记录饮食、当日统计（含热量目标超标判断）、月度日历。
 */
@Service
public class DietService {

    private static final Logger log = LoggerFactory.getLogger(DietService.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserDietRecordRepository dietRepository;
    private final UserHealthProfileRepository profileRepository;

    /**
     * 构造注入依赖。
     *
     * @param dietRepository    饮食记录仓库
     * @param profileRepository 健康档案仓库
     */
    public DietService(UserDietRecordRepository dietRepository,
                       UserHealthProfileRepository profileRepository) {
        this.dietRepository = dietRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * 记录一条饮食数据（餐次/菜品/热量与三大营养素）。
     *
     * @param userId  当前登录用户ID
     * @param request 饮食记录入参
     */
    @Transactional
    public void addRecord(Long userId, DietRecordRequest request) {
        UserDietRecord record = new UserDietRecord();
        record.setUserId(userId);
        record.setRecordDate(request.getRecordDate());
        record.setMealType(request.getMealType());
        record.setRecipeId(request.getRecipeId());
        record.setRecipeName(request.getRecipeName());
        record.setCaloriesKcal(request.getCaloriesKcal());
        record.setCarbsG(request.getCarbsG());
        record.setProteinG(request.getProteinG());
        record.setFatG(request.getFatG());
        dietRepository.save(record);
        log.info("[饮食] 用户{}记录饮食成功: 日期={}, 餐次={}, 菜品={}, 热量={}kcal",
                userId, request.getRecordDate(), request.getMealType(),
                request.getRecipeName(), request.getCaloriesKcal());
    }

    /**
     * 当日饮食统计：汇总总热量/碳水/蛋白/脂肪，并对比健康档案的每日热量目标输出是否超标。
     *
     * @param userId  当前登录用户ID
     * @param dateStr 查询日期字符串（格式 yyyy-MM-dd）
     * @return 当日统计聚合数据
     * @throws BusinessException 日期格式非法时抛 40000
     */
    public DailyDietVO getDaily(Long userId, String dateStr) {
        LocalDate date = parseDate(dateStr);
        List<UserDietRecord> records = dietRepository.findByUserIdAndRecordDateOrderByMealTypeAscIdAsc(userId, date);

        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        List<DailyDietVO.DietRecordVO> recordVOs = new ArrayList<>();
        for (UserDietRecord record : records) {
            totalCalories = totalCalories.add(nullSafe(record.getCaloriesKcal()));
            totalCarbs = totalCarbs.add(nullSafe(record.getCarbsG()));
            totalProtein = totalProtein.add(nullSafe(record.getProteinG()));
            totalFat = totalFat.add(nullSafe(record.getFatG()));

            DailyDietVO.DietRecordVO vo = new DailyDietVO.DietRecordVO();
            vo.setId(record.getId());
            vo.setMealType(record.getMealType());
            vo.setRecipeName(record.getRecipeName());
            vo.setCaloriesKcal(record.getCaloriesKcal());
            vo.setCarbsG(record.getCarbsG());
            vo.setProteinG(record.getProteinG());
            vo.setFatG(record.getFatG());
            recordVOs.add(vo);
        }

        Integer calorieTarget = profileRepository.findByUserId(userId)
                .map(UserHealthProfile::getDailyCalorieTarget)
                .orElse(null);
        // 关键判断：设置了每日热量目标且当日摄入超过目标 → 超标
        boolean exceedTarget = calorieTarget != null
                && totalCalories.compareTo(BigDecimal.valueOf(calorieTarget)) > 0;
        if (exceedTarget) {
            log.info("[饮食] 用户{}当日摄入超标: 日期={}, 摄入={}kcal, 目标={}kcal",
                    userId, date, totalCalories, calorieTarget);
        }

        DailyDietVO vo = new DailyDietVO();
        vo.setRecords(recordVOs);
        vo.setTotalCalories(totalCalories);
        vo.setTotalCarbs(totalCarbs);
        vo.setTotalProtein(totalProtein);
        vo.setTotalFat(totalFat);
        vo.setCalorieTarget(calorieTarget);
        vo.setExceedTarget(exceedTarget);
        return vo;
    }

    /**
     * 月度日历：按天聚合当月每日总热量（仅返回有记录的日期）。
     *
     * @param userId   当前登录用户ID
     * @param monthStr 月份字符串（格式 yyyy-MM）
     * @return 每日总热量列表
     * @throws BusinessException 月份格式非法时抛 40000
     */
    public List<MonthDailyVO> getMonth(Long userId, String monthStr) {
        YearMonth month;
        try {
            month = YearMonth.parse(monthStr);
        } catch (DateTimeParseException e) {
            // 关键判断：月份格式非法
            log.warn("[饮食] 月份格式非法: month={}", monthStr);
            throw new BusinessException(ResultCode.PARAM_ERROR, "月份格式应为yyyy-MM");
        }
        List<Object[]> rows = dietRepository.sumCaloriesGroupByDate(
                userId, month.atDay(1), month.atEndOfMonth());
        List<MonthDailyVO> result = new ArrayList<>();
        for (Object[] row : rows) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal total = (BigDecimal) row[1];
            result.add(new MonthDailyVO(date.format(DATE_FORMATTER), total));
        }
        log.info("[饮食] 用户{}查询月度日历: month={}, 有记录天数={}", userId, monthStr, result.size());
        return result;
    }

    /**
     * 解析日期字符串，非法格式抛参数错误。
     *
     * @param dateStr 日期字符串（yyyy-MM-dd）
     * @return LocalDate
     * @throws BusinessException 日期格式非法时抛 40000
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            // 关键判断：日期格式非法
            log.warn("[饮食] 日期格式非法: date={}", dateStr);
            throw new BusinessException(ResultCode.PARAM_ERROR, "日期格式应为yyyy-MM-dd");
        }
    }

    /**
     * BigDecimal 空安全处理：null 按 0 计。
     *
     * @param value 可能为空的数值
     * @return 非空数值
     */
    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

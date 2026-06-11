package com.foodapp.user.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.user.dto.DietRecordRequest;
import com.foodapp.user.service.DietService;
import com.foodapp.user.vo.DailyDietVO;
import com.foodapp.user.vo.MonthDailyVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 饮食日历接口：记录饮食、当日统计、月度日历（全部需登录）。
 */
@RestController
@RequestMapping("/api/user/diet")
public class DietController {

    private final DietService dietService;

    /**
     * 构造注入饮食业务层。
     *
     * @param dietService 饮食业务层
     */
    public DietController(DietService dietService) {
        this.dietService = dietService;
    }

    /**
     * 记录饮食。
     *
     * @param request 饮食记录入参（日期/餐次/菜品/热量与营养素）
     * @return 成功响应
     */
    @PostMapping
    public Result<Void> addRecord(@Valid @RequestBody DietRecordRequest request) {
        Long userId = UserContext.requireUserId();
        dietService.addRecord(userId, request);
        return Result.success("饮食记录成功", null);
    }

    /**
     * 当日统计：记录列表 + 总热量/碳水/蛋白/脂肪 + 热量目标对比（exceedTarget）。
     *
     * @param date 查询日期（yyyy-MM-dd）
     * @return 当日统计聚合数据
     */
    @GetMapping("/daily")
    public Result<DailyDietVO> getDaily(@RequestParam("date") String date) {
        Long userId = UserContext.requireUserId();
        return Result.success(dietService.getDaily(userId, date));
    }

    /**
     * 月度日历：当月每日总热量列表。
     *
     * @param month 查询月份（yyyy-MM）
     * @return 每日总热量列表
     */
    @GetMapping("/month")
    public Result<List<MonthDailyVO>> getMonth(@RequestParam("month") String month) {
        Long userId = UserContext.requireUserId();
        return Result.success(dietService.getMonth(userId, month));
    }
}

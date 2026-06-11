package com.foodapp.social.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.social.dto.CheckinMonthVO;
import com.foodapp.social.dto.CheckinRequest;
import com.foodapp.social.dto.CheckinResultVO;
import com.foodapp.social.service.CheckinService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 烹饪打卡接口（契约第5节，均需鉴权）。
 * POST /api/social/checkin 打卡；GET /api/social/checkin/my 打卡月历。
 */
@RestController
@RequestMapping("/api/social/checkin")
public class CheckinController {

    private final CheckinService checkinService;

    /**
     * 构造注入打卡服务。
     *
     * @param checkinService 打卡服务
     */
    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    /**
     * 打卡（鉴权）。req: {recipeId?,note?}。计算连续天数并触发徽章。
     * resp data: {continuousDays,newBadges:[{badgeName,icon}]}。同日重复打卡返回 40900。
     *
     * @param request 打卡请求体（可空体字段）
     * @return 连续打卡天数 + 本次新达成徽章
     */
    @PostMapping
    public Result<CheckinResultVO> checkin(@Valid @RequestBody(required = false) CheckinRequest request) {
        Long userId = UserContext.requireUserId();
        return Result.success(checkinService.checkin(userId, request == null ? new CheckinRequest() : request));
    }

    /**
     * 打卡月历（鉴权）。resp data: {dates:["2026-06-01"],continuousDays}。
     *
     * @param month 月份（yyyy-MM，可空默认当月）
     * @return 当月打卡日期列表 + 当前连续打卡天数
     */
    @GetMapping("/my")
    public Result<CheckinMonthVO> my(@RequestParam(required = false) String month) {
        Long userId = UserContext.requireUserId();
        return Result.success(checkinService.myCheckin(userId, month));
    }
}

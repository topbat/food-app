package com.foodapp.social.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.social.dto.BadgeVO;
import com.foodapp.social.service.BadgeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成就徽章接口（契约第5节，需鉴权）。
 * GET /api/social/badge/my 徽章墙（全部徽章 + 是否已获得）。
 */
@RestController
@RequestMapping("/api/social/badge")
public class BadgeController {

    private final BadgeService badgeService;

    /**
     * 构造注入徽章服务。
     *
     * @param badgeService 徽章服务
     */
    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    /**
     * 徽章墙（鉴权）。resp data: [{id,badgeName,badgeDesc,icon,obtained,obtainedAt}]。
     *
     * @return 全部徽章 + 当前用户是否已获得及获得时间
     */
    @GetMapping("/my")
    public Result<List<BadgeVO>> my() {
        Long userId = UserContext.requireUserId();
        return Result.success(badgeService.myBadges(userId));
    }
}

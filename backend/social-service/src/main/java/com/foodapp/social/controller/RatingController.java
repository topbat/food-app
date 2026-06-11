package com.foodapp.social.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.social.dto.RatingRequest;
import com.foodapp.social.dto.RatingVO;
import com.foodapp.social.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜谱评分接口（契约第5节）。
 * POST /api/social/rating 评分（鉴权，重复评分更新原记录）；
 * GET /api/social/rating/{recipeId} 评分统计查询（公开）。
 */
@RestController
@RequestMapping("/api/social/rating")
public class RatingController {

    private final RatingService ratingService;

    /**
     * 构造注入评分服务。
     *
     * @param ratingService 评分服务
     */
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * 评分（鉴权）。req: {recipeId,score}（1-5，重复评分则更新）。
     *
     * @param request 评分请求体
     * @return 成功响应（无数据）
     */
    @PostMapping
    public Result<Void> rate(@Valid @RequestBody RatingRequest request) {
        Long userId = UserContext.requireUserId();
        ratingService.rate(userId, request.getRecipeId(), request.getScore());
        return Result.success();
    }

    /**
     * 评分统计查询（公开）。resp data: {avgScore,ratingCount}。
     *
     * @param recipeId 菜谱ID
     * @return 平均分（1位小数）+ 评分人数
     */
    @GetMapping("/{recipeId}")
    public Result<RatingVO> getRating(@PathVariable Long recipeId) {
        return Result.success(ratingService.getRating(recipeId));
    }
}

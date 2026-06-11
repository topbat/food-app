package com.foodapp.social.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.social.dto.ShareCardRequest;
import com.foodapp.social.dto.ShareCardVO;
import com.foodapp.social.service.ShareCardService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分享卡接口（契约第5节，需鉴权）。
 * POST /api/social/share-card 生成分享卡数据。
 */
@RestController
@RequestMapping("/api/social/share-card")
public class ShareCardController {

    private final ShareCardService shareCardService;

    /**
     * 构造注入分享卡服务。
     *
     * @param shareCardService 分享卡服务
     */
    public ShareCardController(ShareCardService shareCardService) {
        this.shareCardService = shareCardService;
    }

    /**
     * 生成分享卡数据（鉴权）。req: {recipeId?,recipeName?,caloriesKcal?}。
     * resp data: {title,subtitle,calorieText,equivalentText,continuousDays,nickname,date}。
     *
     * @param request 分享卡请求体（可空体字段）
     * @return 分享卡渲染数据
     */
    @PostMapping
    public Result<ShareCardVO> build(@RequestBody(required = false) ShareCardRequest request) {
        Long userId = UserContext.requireUserId();
        return Result.success(shareCardService.buildCard(userId, request == null ? new ShareCardRequest() : request));
    }
}

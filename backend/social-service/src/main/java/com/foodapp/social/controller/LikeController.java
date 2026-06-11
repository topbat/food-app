package com.foodapp.social.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.social.dto.LikeRequest;
import com.foodapp.social.dto.LikeVO;
import com.foodapp.social.service.LikeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞接口（契约第5节）。
 * POST /api/social/like 点赞toggle（鉴权）。
 */
@RestController
@RequestMapping("/api/social/like")
public class LikeController {

    private final LikeService likeService;

    /**
     * 构造注入点赞服务。
     *
     * @param likeService 点赞服务
     */
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    /**
     * 点赞 toggle（鉴权）。req: {targetType,targetId}。resp data: {liked,likeCount}。
     *
     * @param request 点赞请求体
     * @return 本次操作后的点赞状态与点赞总数
     */
    @PostMapping
    public Result<LikeVO> toggle(@Valid @RequestBody LikeRequest request) {
        Long userId = UserContext.requireUserId();
        return Result.success(likeService.toggle(userId, request.getTargetType(), request.getTargetId()));
    }
}

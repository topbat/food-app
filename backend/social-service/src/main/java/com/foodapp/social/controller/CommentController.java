package com.foodapp.social.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.social.dto.CommentCreateRequest;
import com.foodapp.social.dto.CommentVO;
import com.foodapp.social.dto.PageResult;
import com.foodapp.social.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论接口（契约第5节）。
 * POST /api/social/comment 发评论（鉴权）；GET /api/social/comment/list 评论列表（公开）。
 */
@RestController
@RequestMapping("/api/social/comment")
public class CommentController {

    private final CommentService commentService;

    /**
     * 构造注入评论服务。
     *
     * @param commentService 评论服务
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 发评论（鉴权）。req: {targetType,targetId,stepId?,content,parentId?}。
     *
     * @param request 发评论请求体
     * @return 新评论ID
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CommentCreateRequest request) {
        Long userId = UserContext.requireUserId();
        return Result.success(commentService.create(userId, request));
    }

    /**
     * 评论列表（公开），按创建时间倒序。
     * resp list 项: {id,userId,nickname,avatarUrl,stepId,content,parentId,likeCount,createdAt}。
     *
     * @param targetType 评论对象类型（1菜谱 2社区帖子）
     * @param targetId   评论对象ID
     * @param page       页码（默认1）
     * @param size       每页条数（默认10）
     * @return 评论分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<CommentVO>> list(@RequestParam Integer targetType,
                                              @RequestParam Long targetId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.list(targetType, targetId, page, size));
    }
}

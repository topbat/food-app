package com.foodapp.social.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.social.dto.PageResult;
import com.foodapp.social.dto.PostCreateRequest;
import com.foodapp.social.dto.PostCreateVO;
import com.foodapp.social.dto.PostVO;
import com.foodapp.social.service.PostService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子接口（契约第5节，作品墙）。
 * POST /api/social/post 发帖（鉴权）；GET /api/social/post/list 作品墙（公开）；
 * GET /api/social/post/{id} 帖子详情（公开）。
 */
@RestController
@RequestMapping("/api/social/post")
public class PostController {

    private final PostService postService;

    /**
     * 构造注入帖子服务。
     *
     * @param postService 帖子服务
     */
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 发帖（鉴权）。req: {recipeId?,content,imageUrls:[string],postType}。
     * 发帖后自动判断 POST_COUNT 类徽章达成。
     *
     * @param request 发帖请求体
     * @return 新帖子ID + 本次新达成的徽章列表
     */
    @PostMapping
    public Result<PostCreateVO> create(@Valid @RequestBody PostCreateRequest request) {
        Long userId = UserContext.requireUserId();
        return Result.success(postService.create(userId, request));
    }

    /**
     * 作品墙列表（公开），postType 可选过滤，按创建时间倒序。
     * resp list 项: {id,userId,nickname,avatarUrl,recipeId,content,imageUrls:[],postType,likeCount,commentCount,createdAt}。
     *
     * @param postType 帖子类型（可空，1作品晒图 2烹饪打卡 3美食日记）
     * @param page     页码（默认1）
     * @param size     每页条数（默认10）
     * @return 帖子分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<PostVO>> list(@RequestParam(required = false) Integer postType,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(postService.list(postType, page, size));
    }

    /**
     * 帖子详情（公开）。
     *
     * @param id 帖子ID
     * @return 帖子详情 VO
     */
    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.success(postService.detail(id));
    }
}

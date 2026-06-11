package com.foodapp.social.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.social.dto.BadgeBriefVO;
import com.foodapp.social.dto.PageResult;
import com.foodapp.social.dto.PostCreateRequest;
import com.foodapp.social.dto.PostCreateVO;
import com.foodapp.social.dto.PostVO;
import com.foodapp.social.dto.UserPublicVO;
import com.foodapp.social.entity.SocialPost;
import com.foodapp.social.repository.SocialPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 社区帖子服务（作品墙）。
 * 发帖（imageUrls 列表与 TEXT JSON 字符串互转、触发 POST_COUNT 徽章）、列表、详情。
 */
@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    /** 正常状态 */
    private static final int STATUS_NORMAL = 1;

    /** 发帖数类徽章条件类型 */
    private static final String CONDITION_POST_COUNT = "POST_COUNT";

    private final SocialPostRepository postRepository;
    private final UserInfoService userInfoService;
    private final BadgeService badgeService;
    private final ObjectMapper objectMapper;

    /**
     * 构造注入帖子仓储、用户信息服务、徽章服务与 Jackson ObjectMapper。
     *
     * @param postRepository  帖子仓储
     * @param userInfoService 用户公开信息服务（装配昵称头像）
     * @param badgeService    徽章服务（发帖后判断 POST_COUNT 徽章）
     * @param objectMapper    Jackson ObjectMapper（imageUrls 列表与 JSON 字符串互转）
     */
    public PostService(SocialPostRepository postRepository,
                       UserInfoService userInfoService,
                       BadgeService badgeService,
                       ObjectMapper objectMapper) {
        this.postRepository = postRepository;
        this.userInfoService = userInfoService;
        this.badgeService = badgeService;
        this.objectMapper = objectMapper;
    }

    /**
     * 发帖：imageUrls 列表序列化为 JSON 字符串存 TEXT 列；
     * 发帖成功后统计用户累计发帖数并同事务判断 POST_COUNT 类徽章达成。
     *
     * @param userId  当前登录用户ID
     * @param request 发帖请求体
     * @return 新帖子ID + 本次新达成的徽章列表
     */
    @Transactional
    public PostCreateVO create(Long userId, PostCreateRequest request) {
        SocialPost post = new SocialPost();
        post.setUserId(userId);
        post.setRecipeId(request.getRecipeId());
        post.setContent(request.getContent());
        post.setImageUrls(writeImageUrls(request.getImageUrls()));
        post.setPostType(request.getPostType());
        postRepository.save(post);
        log.info("[帖子] 用户{}发帖成功, postId={}, postType={}", userId, post.getId(), post.getPostType());

        // 关键判断：发帖后统计累计发帖数，判断 POST_COUNT 类徽章是否达成
        long postCount = postRepository.countByUserIdAndStatus(userId, STATUS_NORMAL);
        List<BadgeBriefVO> newBadges = badgeService.awardBadges(userId, CONDITION_POST_COUNT, (int) postCount);
        return new PostCreateVO(post.getId(), newBadges);
    }

    /**
     * 作品墙分页列表：仅正常状态（status=1），postType 可选过滤，按创建时间倒序，装配用户昵称头像。
     *
     * @param postType 帖子类型（可空，1作品晒图 2烹饪打卡 3美食日记）
     * @param page     页码（从1开始）
     * @param size     每页条数
     * @return 帖子分页结果
     */
    public PageResult<PostVO> list(Integer postType, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        // 关键判断：postType 为空查全部类型，否则按类型过滤
        Page<SocialPost> postPage = postType == null
                ? postRepository.findByStatus(STATUS_NORMAL, pageable)
                : postRepository.findByPostTypeAndStatus(postType, STATUS_NORMAL, pageable);
        List<Long> userIds = postPage.getContent().stream().map(SocialPost::getUserId).toList();
        Map<Long, UserPublicVO> userMap = userInfoService.getUsers(userIds);
        List<PostVO> list = postPage.getContent().stream()
                .map(post -> toVO(post, userMap.get(post.getUserId())))
                .toList();
        return PageResult.of(postPage.getTotalElements(), page, size, list);
    }

    /**
     * 帖子详情（仅正常状态可见），装配用户昵称头像。
     *
     * @param id 帖子ID
     * @return 帖子详情 VO
     * @throws BusinessException 帖子不存在或已删除时抛出
     */
    public PostVO detail(Long id) {
        SocialPost post = postRepository.findById(id)
                .filter(p -> p.getStatus() == STATUS_NORMAL)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "帖子不存在"));
        return toVO(post, userInfoService.getUser(post.getUserId()));
    }

    /**
     * 实体转 VO（imageUrls JSON 字符串还原为列表，装配昵称头像）。
     *
     * @param post 帖子实体
     * @param user 发布用户公开信息（含降级兜底，永不为 null）
     * @return 帖子 VO
     */
    private PostVO toVO(SocialPost post, UserPublicVO user) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRecipeId(post.getRecipeId());
        vo.setContent(post.getContent());
        vo.setImageUrls(readImageUrls(post.getImageUrls()));
        vo.setPostType(post.getPostType());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCreatedAt(post.getCreatedAt());
        return vo;
    }

    /**
     * 图片URL列表序列化为 JSON 数组字符串（null 视为空列表）。
     *
     * @param imageUrls 图片URL列表
     * @return JSON 数组字符串（如 ["url1","url2"]）
     */
    private String writeImageUrls(List<String> imageUrls) {
        try {
            return objectMapper.writeValueAsString(imageUrls == null ? List.of() : imageUrls);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片列表格式非法");
        }
    }

    /**
     * JSON 数组字符串还原为图片URL列表（空值或解析失败时降级为空列表，保证列表可用）。
     *
     * @param json TEXT 列中的 JSON 数组字符串
     * @return 图片URL列表（永不为 null）
     */
    private List<String> readImageUrls(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            // 关键判断：脏数据解析失败不影响列表展示，降级为空列表
            log.warn("[帖子] imageUrls JSON解析失败, 降级为空列表, json={}", json);
            return List.of();
        }
    }
}

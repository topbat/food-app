package com.foodapp.social.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.social.dto.CommentCreateRequest;
import com.foodapp.social.dto.CommentVO;
import com.foodapp.social.dto.PageResult;
import com.foodapp.social.dto.UserPublicVO;
import com.foodapp.social.entity.SocialComment;
import com.foodapp.social.entity.SocialPost;
import com.foodapp.social.repository.SocialCommentRepository;
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
 * 评论服务。
 * 发评论（评论帖子时同事务维护帖子评论数）、分页评论列表（装配用户昵称头像）。
 */
@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    /** 评论对象类型：2 社区帖子 */
    private static final int TARGET_TYPE_POST = 2;

    /** 正常状态 */
    private static final int STATUS_NORMAL = 1;

    private final SocialCommentRepository commentRepository;
    private final SocialPostRepository postRepository;
    private final UserInfoService userInfoService;

    /**
     * 构造注入评论仓储、帖子仓储与用户信息服务。
     *
     * @param commentRepository 评论仓储
     * @param postRepository    帖子仓储（评论帖子时维护评论数）
     * @param userInfoService   用户公开信息服务（装配昵称头像）
     */
    public CommentService(SocialCommentRepository commentRepository,
                          SocialPostRepository postRepository,
                          UserInfoService userInfoService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userInfoService = userInfoService;
    }

    /**
     * 发评论：targetType=2（评论帖子）时同事务为帖子 comment_count + 1。
     *
     * @param userId  当前登录用户ID
     * @param request 发评论请求体
     * @return 新评论ID
     * @throws BusinessException 评论的帖子不存在或已删除时抛出
     */
    @Transactional
    public Long create(Long userId, CommentCreateRequest request) {
        // 关键判断：评论帖子时校验帖子存在，并同事务维护帖子评论数
        if (request.getTargetType() == TARGET_TYPE_POST) {
            SocialPost post = postRepository.findById(request.getTargetId())
                    .filter(p -> p.getStatus() == STATUS_NORMAL)
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "评论的帖子不存在"));
            post.setCommentCount(post.getCommentCount() + 1);
            postRepository.save(post);
        }
        SocialComment comment = new SocialComment();
        comment.setTargetType(request.getTargetType());
        comment.setTargetId(request.getTargetId());
        comment.setStepId(request.getStepId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        commentRepository.save(comment);
        log.info("[评论] 用户{}发表评论成功, commentId={}, targetType={}, targetId={}",
                userId, comment.getId(), request.getTargetType(), request.getTargetId());
        return comment.getId();
    }

    /**
     * 分页查询评论列表（按创建时间倒序），并装配评论用户昵称头像。
     *
     * @param targetType 评论对象类型（1菜谱 2社区帖子）
     * @param targetId   评论对象ID
     * @param page       页码（从1开始）
     * @param size       每页条数
     * @return 评论分页结果
     */
    public PageResult<CommentVO> list(Integer targetType, Long targetId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Page<SocialComment> commentPage =
                commentRepository.findByTargetTypeAndTargetIdAndStatus(targetType, targetId, STATUS_NORMAL, pageable);
        List<Long> userIds = commentPage.getContent().stream().map(SocialComment::getUserId).toList();
        Map<Long, UserPublicVO> userMap = userInfoService.getUsers(userIds);
        List<CommentVO> list = commentPage.getContent().stream()
                .map(comment -> toVO(comment, userMap.get(comment.getUserId())))
                .toList();
        return PageResult.of(commentPage.getTotalElements(), page, size, list);
    }

    /**
     * 实体转列表项 VO（装配昵称头像）。
     *
     * @param comment 评论实体
     * @param user    评论用户公开信息（含降级兜底，永不为 null）
     * @return 评论列表项 VO
     */
    private CommentVO toVO(SocialComment comment, UserPublicVO user) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStepId(comment.getStepId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }
}

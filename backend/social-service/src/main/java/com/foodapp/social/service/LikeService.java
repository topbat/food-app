package com.foodapp.social.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.social.dto.LikeVO;
import com.foodapp.social.entity.SocialComment;
import com.foodapp.social.entity.SocialLikeRecord;
import com.foodapp.social.entity.SocialPost;
import com.foodapp.social.repository.SocialCommentRepository;
import com.foodapp.social.repository.SocialLikeRecordRepository;
import com.foodapp.social.repository.SocialPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 点赞服务（toggle 模式）。
 * 已点赞则取消、未点赞则点赞；帖子/评论同步维护对应表 like_count，
 * 菜谱（targetType=3）仅记录点赞流水，计数按记录表统计。
 */
@Service
public class LikeService {

    private static final Logger log = LoggerFactory.getLogger(LikeService.class);

    /** 点赞对象类型：1 帖子 */
    private static final int TARGET_TYPE_POST = 1;
    /** 点赞对象类型：2 评论 */
    private static final int TARGET_TYPE_COMMENT = 2;
    /** 点赞对象类型：3 菜谱 */
    private static final int TARGET_TYPE_RECIPE = 3;

    private final SocialLikeRecordRepository likeRecordRepository;
    private final SocialPostRepository postRepository;
    private final SocialCommentRepository commentRepository;

    /**
     * 构造注入点赞记录、帖子与评论仓储。
     *
     * @param likeRecordRepository 点赞记录仓储
     * @param postRepository       帖子仓储（同步维护帖子 like_count）
     * @param commentRepository    评论仓储（同步维护评论 like_count）
     */
    public LikeService(SocialLikeRecordRepository likeRecordRepository,
                       SocialPostRepository postRepository,
                       SocialCommentRepository commentRepository) {
        this.likeRecordRepository = likeRecordRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * 点赞 toggle：
     * 已存在点赞记录 → 删除记录并计数 -1，liked=false；
     * 不存在 → 插入记录并计数 +1，liked=true。
     * 点赞记录与计数更新在同一事务内完成。
     *
     * @param userId     当前登录用户ID
     * @param targetType 点赞对象类型（1帖子 2评论 3菜谱）
     * @param targetId   点赞对象ID
     * @return 本次操作后的点赞状态与该对象当前点赞总数
     * @throws BusinessException 帖子/评论不存在时抛出
     */
    @Transactional
    public LikeVO toggle(Long userId, Integer targetType, Long targetId) {
        Optional<SocialLikeRecord> existing =
                likeRecordRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        boolean liked;
        // 关键判断：toggle 分支——已点赞则取消，未点赞则新增
        if (existing.isPresent()) {
            likeRecordRepository.delete(existing.get());
            liked = false;
        } else {
            SocialLikeRecord record = new SocialLikeRecord();
            record.setUserId(userId);
            record.setTargetType(targetType);
            record.setTargetId(targetId);
            likeRecordRepository.save(record);
            liked = true;
        }
        long likeCount = applyCount(targetType, targetId, liked ? 1 : -1);
        log.info("[点赞] 用户{} toggle结果: targetType={}, targetId={}, liked={}, likeCount={}",
                userId, targetType, targetId, liked, likeCount);
        return new LikeVO(liked, likeCount);
    }

    /**
     * 同步更新点赞对象的计数并返回最新点赞总数。
     * 帖子/评论更新自身表 like_count 字段（不小于0）；菜谱无本地表，按点赞记录统计。
     *
     * @param targetType 点赞对象类型
     * @param targetId   点赞对象ID
     * @param delta      计数增量（+1 点赞 / -1 取消）
     * @return 该对象当前点赞总数
     * @throws BusinessException 帖子/评论不存在时抛出
     */
    private long applyCount(Integer targetType, Long targetId, int delta) {
        switch (targetType) {
            case TARGET_TYPE_POST -> {
                SocialPost post = postRepository.findById(targetId)
                        .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "点赞的帖子不存在"));
                post.setLikeCount(Math.max(0, post.getLikeCount() + delta));
                postRepository.save(post);
                return post.getLikeCount();
            }
            case TARGET_TYPE_COMMENT -> {
                SocialComment comment = commentRepository.findById(targetId)
                        .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "点赞的评论不存在"));
                comment.setLikeCount(Math.max(0, comment.getLikeCount() + delta));
                commentRepository.save(comment);
                return comment.getLikeCount();
            }
            case TARGET_TYPE_RECIPE -> {
                // 关键判断：菜谱点赞仅记录流水，计数直接按记录表统计
                return likeRecordRepository.countByTargetTypeAndTargetId(targetType, targetId);
            }
            default -> throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的点赞对象类型");
        }
    }
}

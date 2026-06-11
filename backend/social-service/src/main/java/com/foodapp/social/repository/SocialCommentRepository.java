package com.foodapp.social.repository;

import com.foodapp.social.entity.SocialComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 评论仓储。
 */
public interface SocialCommentRepository extends JpaRepository<SocialComment, Long> {

    /**
     * 分页查询某对象（菜谱或帖子）下的正常状态评论。
     *
     * @param targetType 评论对象类型（1菜谱 2社区帖子）
     * @param targetId   评论对象ID
     * @param status     状态（1正常）
     * @param pageable   分页参数
     * @return 评论分页
     */
    Page<SocialComment> findByTargetTypeAndTargetIdAndStatus(Integer targetType, Long targetId, Integer status, Pageable pageable);
}

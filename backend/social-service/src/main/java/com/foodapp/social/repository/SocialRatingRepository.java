package com.foodapp.social.repository;

import com.foodapp.social.entity.SocialRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 菜谱评分仓储。
 */
public interface SocialRatingRepository extends JpaRepository<SocialRating, Long> {

    /**
     * 查询某用户对某菜谱的评分记录（用于重复评分时更新原记录）。
     *
     * @param userId   用户ID
     * @param recipeId 菜谱ID
     * @return 评分记录（可能不存在）
     */
    Optional<SocialRating> findByUserIdAndRecipeId(Long userId, Long recipeId);

    /**
     * 计算某菜谱的平均评分。
     *
     * @param recipeId 菜谱ID
     * @return 平均分；无评分时返回 null
     */
    @Query("select avg(r.score) from SocialRating r where r.recipeId = :recipeId")
    Double avgScoreByRecipeId(@Param("recipeId") Long recipeId);

    /**
     * 统计某菜谱的评分人数。
     *
     * @param recipeId 菜谱ID
     * @return 评分人数
     */
    long countByRecipeId(Long recipeId);
}

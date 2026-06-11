package com.foodapp.social.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.social.dto.RatingVO;
import com.foodapp.social.entity.SocialRating;
import com.foodapp.social.repository.SocialRatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 菜谱评分服务。
 * 同一用户对同一菜谱重复评分时更新原记录；查询返回平均分（1位小数）与评分人数。
 */
@Service
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    private final SocialRatingRepository ratingRepository;

    /**
     * 构造注入评分仓储。
     *
     * @param ratingRepository 评分仓储
     */
    public RatingService(SocialRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    /**
     * 评分：score 必须在 1-5 之间；已评分过则更新原记录，否则新增记录。
     *
     * @param userId   当前登录用户ID
     * @param recipeId 菜谱ID
     * @param score    评分（1-5星）
     * @throws BusinessException score 越界时抛出（40000）
     */
    @Transactional
    public void rate(Long userId, Long recipeId, Integer score) {
        // 关键判断：评分范围防御性校验（Bean Validation 之外的兜底）
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评分必须在1-5星之间");
        }
        Optional<SocialRating> existing = ratingRepository.findByUserIdAndRecipeId(userId, recipeId);
        // 关键判断：重复评分更新原记录，不产生新行
        if (existing.isPresent()) {
            SocialRating rating = existing.get();
            log.info("[评分] 用户{}重复评分, recipeId={}, 原评分{}星 -> 新评分{}星",
                    userId, recipeId, rating.getScore(), score);
            rating.setScore(score);
            ratingRepository.save(rating);
            return;
        }
        SocialRating rating = new SocialRating();
        rating.setUserId(userId);
        rating.setRecipeId(recipeId);
        rating.setScore(score);
        ratingRepository.save(rating);
        log.info("[评分] 用户{}首次评分成功, recipeId={}, score={}星", userId, recipeId, score);
    }

    /**
     * 查询菜谱评分统计：平均分保留1位小数（无评分时为0.0）+ 评分人数。
     *
     * @param recipeId 菜谱ID
     * @return 评分统计 VO
     */
    public RatingVO getRating(Long recipeId) {
        Double avg = ratingRepository.avgScoreByRecipeId(recipeId);
        long count = ratingRepository.countByRecipeId(recipeId);
        double avgScore = avg == null ? 0.0 : Math.round(avg * 10) / 10.0;
        return new RatingVO(avgScore, count);
    }
}

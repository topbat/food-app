package com.foodapp.recipe.repository;

import com.foodapp.recipe.entity.RecipeSubstituteRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 智能食材替换规则仓库。
 */
public interface RecipeSubstituteRuleRepository extends JpaRepository<RecipeSubstituteRule, Long> {

    /**
     * 按「菜谱所含食材 + 适用场景」匹配替换规则。
     *
     * @param sourceIngredientIds 菜谱中的食材ID集合
     * @param scene               适用场景（减脂/控糖/素食/低嘌呤）
     * @return 命中的替换规则
     */
    List<RecipeSubstituteRule> findBySourceIngredientIdInAndScene(Collection<Long> sourceIngredientIds, String scene);
}

package com.foodapp.recipe.repository;

import com.foodapp.recipe.entity.RecipeTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 菜谱标签关联仓库（relation_type：1适宜 2慎用/禁忌）。
 */
public interface RecipeTagRelationRepository extends JpaRepository<RecipeTagRelation, Long> {

    /**
     * 查询某菜谱的全部标签关联。
     *
     * @param recipeId 菜谱ID
     * @return 关联列表
     */
    List<RecipeTagRelation> findByRecipeId(Long recipeId);

    /**
     * 批量查询多个菜谱的标签关联（列表页一次装配，避免 N+1 查询，性能要求）。
     *
     * @param recipeIds 菜谱ID集合
     * @return 关联列表
     */
    List<RecipeTagRelation> findByRecipeIdIn(Collection<Long> recipeIds);
}

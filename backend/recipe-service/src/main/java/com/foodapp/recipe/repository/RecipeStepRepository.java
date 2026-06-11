package com.foodapp.recipe.repository;

import com.foodapp.recipe.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 菜谱结构化步骤仓库（五步法：PREPARE/WASH/CUT/COOK/PLATE）。
 */
public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {

    /**
     * 查询某菜谱全部步骤（阶段顺序由业务层按五步法权重排序）。
     *
     * @param recipeId 菜谱ID
     * @return 步骤列表
     */
    List<RecipeStep> findByRecipeIdOrderByStepIndexAsc(Long recipeId);
}

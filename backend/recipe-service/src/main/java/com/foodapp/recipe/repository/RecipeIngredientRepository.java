package com.foodapp.recipe.repository;

import com.foodapp.recipe.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 菜谱食材关联仓库。
 */
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    /**
     * 查询某菜谱的全部食材用量记录。
     *
     * @param recipeId 菜谱ID
     * @return 食材关联列表
     */
    List<RecipeIngredient> findByRecipeId(Long recipeId);
}

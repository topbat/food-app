package com.foodapp.recipe.repository;

import com.foodapp.recipe.entity.RecipeIngredientLib;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 基础食材库仓库（含每100克营养成分）。
 */
public interface RecipeIngredientLibRepository extends JpaRepository<RecipeIngredientLib, Long> {

    /**
     * 按食材分类排序查询全量食材库（前端"冰箱清理模式"按分类分组展示）。
     *
     * @return 食材库列表
     */
    List<RecipeIngredientLib> findAllByOrderByCategoryAscIdAsc();
}

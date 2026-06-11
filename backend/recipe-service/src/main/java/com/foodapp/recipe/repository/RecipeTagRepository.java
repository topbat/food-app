package com.foodapp.recipe.repository;

import com.foodapp.recipe.entity.RecipeTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 标签字典仓库（1人群 2功效 3场景）。
 */
public interface RecipeTagRepository extends JpaRepository<RecipeTag, Long> {

    /**
     * 按标签名精确查询（列表筛选时把标签名换算为标签ID）。
     *
     * @param tagName 标签名称
     * @return 标签
     */
    Optional<RecipeTag> findByTagName(String tagName);

    /**
     * 按类型排序查询全量标签字典。
     *
     * @return 标签列表
     */
    List<RecipeTag> findAllByOrderByTagTypeAscIdAsc();
}

package com.foodapp.recipe.repository;

import com.foodapp.recipe.entity.RecipeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 菜谱主表仓库。
 * 支持 Specification 动态组合查询（菜系/难度/关键词/热量/标签/食材多条件自由组合）。
 */
public interface RecipeInfoRepository extends JpaRepository<RecipeInfo, Long>, JpaSpecificationExecutor<RecipeInfo> {

    /**
     * 浏览数原子 +1（数据库级 UPDATE，避免并发读改写覆盖）。
     *
     * @param id 菜谱ID
     * @return 受影响行数
     */
    @Modifying
    @Query("UPDATE RecipeInfo r SET r.viewCount = r.viewCount + 1 WHERE r.id = :id")
    int incrementViewCount(@Param("id") Long id);
}

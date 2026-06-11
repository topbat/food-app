package com.foodapp.search.repository;

import com.foodapp.search.entity.SearchHotKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 热搜关键词数据访问层。
 */
@Repository
public interface SearchHotKeywordRepository extends JpaRepository<SearchHotKeyword, Long> {

    /**
     * 按关键词精确查询（关键词全表唯一）。
     *
     * @param keyword 关键词
     * @return 热搜记录（不存在时为空）
     */
    Optional<SearchHotKeyword> findByKeyword(String keyword);

    /**
     * 查询热搜 TOP10（按累计搜索次数降序）。
     *
     * @return 热搜关键词列表
     */
    List<SearchHotKeyword> findTop10ByOrderBySearchCountDesc();
}

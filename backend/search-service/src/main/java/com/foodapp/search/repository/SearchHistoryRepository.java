package com.foodapp.search.repository;

import com.foodapp.search.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 搜索历史数据访问层。
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /**
     * 查询指定用户最近 20 条搜索历史（按创建时间倒序）。
     *
     * @param userId 用户ID
     * @return 搜索历史列表
     */
    List<SearchHistory> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 删除指定用户的全部搜索历史。
     *
     * @param userId 用户ID
     * @return 删除条数
     */
    long deleteByUserId(Long userId);
}

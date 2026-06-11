package com.foodapp.search.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.search.client.RecipeClient;
import com.foodapp.search.entity.SearchHistory;
import com.foodapp.search.entity.SearchHotKeyword;
import com.foodapp.search.repository.SearchHistoryRepository;
import com.foodapp.search.repository.SearchHotKeywordRepository;
import com.foodapp.search.vo.HotKeywordVO;
import com.foodapp.search.vo.SearchHistoryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索业务服务。
 * 职责：搜索条件透传菜谱服务、热搜计数与 TOP10 榜单（30秒内存缓存）、登录用户搜索历史管理。
 */
@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    /** 热搜 TOP10 缓存有效期（毫秒）：30 秒，避免高频查询击穿数据库；prod 可换 Redis 共享缓存 */
    private static final long HOT_CACHE_TTL_MS = 30_000L;

    private final RecipeClient recipeClient;
    private final SearchHistoryRepository historyRepository;
    private final SearchHotKeywordRepository hotKeywordRepository;

    /** 热搜 TOP10 简单内存缓存（单机有效；prod 高可用场景建议替换为 Redis ZSet） */
    private volatile List<HotKeywordVO> hotCache = List.of();
    /** 热搜缓存过期时间戳（毫秒） */
    private volatile long hotCacheExpireAt = 0L;
    /** 热搜缓存刷新锁，避免并发重复回源 */
    private final Object hotCacheLock = new Object();

    /**
     * 构造注入依赖。
     *
     * @param recipeClient         菜谱服务客户端
     * @param historyRepository    搜索历史仓库
     * @param hotKeywordRepository 热搜关键词仓库
     */
    public SearchService(RecipeClient recipeClient,
                         SearchHistoryRepository historyRepository,
                         SearchHotKeywordRepository hotKeywordRepository) {
        this.recipeClient = recipeClient;
        this.historyRepository = historyRepository;
        this.hotKeywordRepository = hotKeywordRepository;
    }

    /**
     * 搜索菜谱：把查询条件原样透传给菜谱服务 GET /api/recipe/list，并返回其 data。
     * keyword 非空时做热搜计数；登录用户额外写入搜索历史。
     *
     * @param page        页码
     * @param size        每页条数
     * @param cuisineType 菜系（可空）
     * @param difficulty  难度（可空）
     * @param keyword     关键词（可空）
     * @param maxCalories 最大热量（可空）
     * @param tagName     人群标签名（可空）
     * @param ingredients 食材列表，逗号分隔（可空，冰箱清理模式）
     * @param searchType  搜索类型（1关键词 2按食材 3按营养目标 4按人群）
     * @param userId      当前登录用户ID（游客为 null）
     * @return 菜谱服务返回的分页结构 {total,page,size,list}
     * @throws BusinessException 菜谱服务调用失败时抛出 REMOTE_ERROR
     */
    public Map<String, Object> searchRecipes(Integer page, Integer size, String cuisineType, String difficulty,
                                             String keyword, Integer maxCalories, String tagName, String ingredients,
                                             Integer searchType, Long userId) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        putIfNotBlank(params, "cuisineType", cuisineType);
        putIfNotBlank(params, "difficulty", difficulty);
        putIfNotBlank(params, "keyword", keyword);
        if (maxCalories != null) {
            params.put("maxCalories", String.valueOf(maxCalories));
        }
        putIfNotBlank(params, "tagName", tagName);
        putIfNotBlank(params, "ingredients", ingredients);

        Map<String, Object> data;
        try {
            data = recipeClient.listRecipes(params);
        } catch (Exception e) {
            // 关键判断：搜索是核心链路，下游失败直接报错（区别于推荐接口的降级策略）
            log.error("[搜索] 调用菜谱服务失败: 参数={}, 原因={}", params, e.getMessage(), e);
            throw new BusinessException(ResultCode.REMOTE_ERROR, "菜谱服务调用失败，请稍后重试");
        }

        // 关键判断：仅在关键词非空且下游搜索成功后做热搜计数与历史记录
        if (keyword != null && !keyword.isBlank()) {
            recordKeyword(keyword.trim(), searchType, userId);
        }
        return data;
    }

    /**
     * 记录关键词：热搜表 upsert（存在则计数+1，否则新建），登录用户额外写搜索历史。
     *
     * @param keyword    搜索关键词（已 trim）
     * @param searchType 搜索类型
     * @param userId     当前登录用户ID（游客为 null，只计热搜不写历史）
     */
    @Transactional
    public void recordKeyword(String keyword, Integer searchType, Long userId) {
        // 关键判断：热搜计数 upsert —— 存在则 search_count+1，否则新建首条记录
        SearchHotKeyword hot = hotKeywordRepository.findByKeyword(keyword).orElse(null);
        if (hot == null) {
            hot = new SearchHotKeyword();
            hot.setKeyword(keyword);
            hot.setSearchCount(1L);
        } else {
            hot.setSearchCount(hot.getSearchCount() + 1);
        }
        hotKeywordRepository.save(hot);
        log.info("热搜计数: {} -> {}", keyword, hot.getSearchCount());

        // 关键判断：仅登录用户写搜索历史（游客无历史归属）
        if (userId != null) {
            SearchHistory history = new SearchHistory();
            history.setUserId(userId);
            history.setKeyword(keyword);
            history.setSearchType(searchType);
            historyRepository.save(history);
            log.info("[搜索历史] 已记录: userId={}, keyword={}, searchType={}", userId, keyword, searchType);
        }
    }

    /**
     * 查询热搜 TOP10（按累计搜索次数降序）。
     * 性能优化：结果做 30 秒简单内存缓存，避免热搜榜被高频刷新请求击穿；prod 可替换为 Redis。
     *
     * @return 热搜榜单（最多 10 条）
     */
    public List<HotKeywordVO> hotTop10() {
        long now = System.currentTimeMillis();
        // 关键判断：缓存未过期直接返回，不回源数据库
        if (now < hotCacheExpireAt) {
            log.info("[热搜] 命中30秒内存缓存, 条数={}", hotCache.size());
            return hotCache;
        }
        synchronized (hotCacheLock) {
            // 双重检查：并发场景下只允许一个线程回源刷新
            if (System.currentTimeMillis() < hotCacheExpireAt) {
                return hotCache;
            }
            List<HotKeywordVO> list = hotKeywordRepository.findTop10ByOrderBySearchCountDesc().stream()
                    .map(h -> new HotKeywordVO(h.getKeyword(), h.getSearchCount()))
                    .toList();
            hotCache = list;
            hotCacheExpireAt = System.currentTimeMillis() + HOT_CACHE_TTL_MS;
            log.info("[热搜] 刷新TOP10缓存, 条数={}", list.size());
            return list;
        }
    }

    /**
     * 查询当前用户最近 20 条搜索历史（按时间倒序）。
     *
     * @param userId 当前登录用户ID
     * @return 搜索历史列表
     */
    public List<SearchHistoryVO> recentHistory(Long userId) {
        List<SearchHistoryVO> list = historyRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(h -> new SearchHistoryVO(h.getId(), h.getKeyword(), h.getSearchType(), h.getCreatedAt()))
                .toList();
        log.info("[搜索历史] 查询最近历史: userId={}, 条数={}", userId, list.size());
        return list;
    }

    /**
     * 清空当前用户的全部搜索历史。
     *
     * @param userId 当前登录用户ID
     * @return 清除条数
     */
    @Transactional
    public long clearHistory(Long userId) {
        long count = historyRepository.deleteByUserId(userId);
        log.info("[搜索历史] 清空历史: userId={}, 清除条数={}", userId, count);
        return count;
    }

    /**
     * 工具方法：值非空白时放入参数表。
     *
     * @param params 参数表
     * @param key    参数名
     * @param value  参数值
     */
    private void putIfNotBlank(Map<String, String> params, String key, String value) {
        if (value != null && !value.isBlank()) {
            params.put(key, value);
        }
    }
}

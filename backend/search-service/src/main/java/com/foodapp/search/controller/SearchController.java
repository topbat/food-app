package com.foodapp.search.controller;

import com.foodapp.common.auth.JwtUtil;
import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.search.service.RecommendService;
import com.foodapp.search.service.SearchService;
import com.foodapp.search.vo.HotKeywordVO;
import com.foodapp.search.vo.RecommendVO;
import com.foodapp.search.vo.SearchHistoryVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 搜索与推荐接口（前缀 /api/search）。
 * 鉴权说明：/history** 由 AuthInterceptor 强制登录；
 * /recipes 与 /recommend 为"可选登录"——手动读 Authorization 头，解析失败一律按游客处理，不报错。
 */
@RestController
@RequestMapping("/api/search")
@Validated
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;
    private final RecommendService recommendService;
    private final JwtUtil jwtUtil;

    /**
     * 构造注入依赖。
     *
     * @param searchService    搜索业务服务
     * @param recommendService 个性化推荐服务
     * @param jwtUtil          JWT 工具（可选登录接口手动解析 Token）
     */
    public SearchController(SearchService searchService, RecommendService recommendService, JwtUtil jwtUtil) {
        this.searchService = searchService;
        this.recommendService = recommendService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 搜索菜谱（可选登录）：条件原样透传菜谱服务 /api/recipe/list 并返回其分页结果。
     * keyword 非空时热搜计数 +1；登录用户额外写搜索历史。
     *
     * @param page          页码（默认 1）
     * @param size          每页条数（默认 10）
     * @param cuisineType   菜系（可空）
     * @param difficulty    难度（可空）
     * @param keyword       关键词（可空）
     * @param maxCalories   最大热量（可空）
     * @param tagName       人群标签名（可空）
     * @param ingredients   食材列表，逗号分隔（可空，冰箱清理模式）
     * @param searchType    搜索类型（1关键词 2按食材 3按营养目标 4按人群，默认 1）
     * @param authorization Authorization 头（可空，带合法 Token 时记录搜索历史）
     * @return 菜谱分页结构 {total,page,size,list:[RecipeSummary]}
     */
    @GetMapping("/recipes")
    public Result<Map<String, Object>> searchRecipes(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页最少1条") @Max(value = 50, message = "每页最多50条") Integer size,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Min(value = 1, message = "最大热量必须为正数") Integer maxCalories,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String ingredients,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "搜索类型取值1-4") @Max(value = 4, message = "搜索类型取值1-4") Integer searchType,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = resolveOptionalUserId(authorization);
        // 关键判断：登录态区分 —— 登录用户搜索会记录历史，游客只计热搜
        if (userId != null) {
            log.info("[搜索] 登录用户搜索: userId={}, keyword={}, searchType={}", userId, keyword, searchType);
        } else {
            log.info("[搜索] 游客搜索: keyword={}, searchType={}", keyword, searchType);
        }
        return Result.success(searchService.searchRecipes(page, size, cuisineType, difficulty,
                keyword, maxCalories, tagName, ingredients, searchType, userId));
    }

    /**
     * 热搜 TOP10（公开）：按累计搜索次数降序，结果带 30 秒内存缓存。
     *
     * @return 热搜榜单 [{keyword,searchCount}]
     */
    @GetMapping("/hot")
    public Result<List<HotKeywordVO>> hot() {
        return Result.success(searchService.hotTop10());
    }

    /**
     * 我的搜索历史（需登录）：最近 20 条按时间倒序。
     *
     * @return 搜索历史 [{id,keyword,searchType,createdAt}]
     */
    @GetMapping("/history")
    public Result<List<SearchHistoryVO>> history() {
        Long userId = UserContext.requireUserId();
        return Result.success(searchService.recentHistory(userId));
    }

    /**
     * 清空我的搜索历史（需登录）。
     *
     * @return 成功响应（无数据）
     */
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        Long userId = UserContext.requireUserId();
        searchService.clearHistory(userId);
        return Result.success("搜索历史已清空", null);
    }

    /**
     * 个性化推荐（可选登录）：
     * 登录用户按 Z 世代标签映射菜谱标签聚合推荐；游客/无标签/下游失败按热度推荐（永不报错）。
     *
     * @param limit         推荐条数上限（默认 10，最大 50）
     * @param authorization Authorization 头（可空，带合法 Token 时走个性化策略并原样转发给用户服务）
     * @return 推荐结果 {reason,list:[RecipeSummary]}
     */
    @GetMapping("/recommend")
    public Result<RecommendVO> recommend(
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "推荐条数最少1条") @Max(value = 50, message = "推荐条数最多50条") Integer limit,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = resolveOptionalUserId(authorization);
        // 关键判断：登录态区分 —— 决定走个性化策略还是游客热度策略
        if (userId != null) {
            log.info("[推荐] 登录用户搜索推荐: userId={}, limit={}", userId, limit);
        } else {
            log.info("[推荐] 游客搜索推荐: limit={}", limit);
        }
        return Result.success(recommendService.recommend(userId, authorization, limit));
    }

    /**
     * 可选登录解析：手动读取 Authorization 头并解析用户ID。
     * 解析失败（缺头/格式错/Token过期或伪造）一律按游客处理，不报错（与强制鉴权接口的 401 行为区分）。
     *
     * @param authorization Authorization 头原始值（可能为 null）
     * @return 用户ID；游客或解析失败返回 null
     */
    private Long resolveOptionalUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        Long userId = jwtUtil.getUserId(authorization.substring(7));
        // 关键判断：Token 非法/过期时按游客降级处理，可选登录接口不返回 401
        if (userId == null) {
            log.warn("[可选登录] Token解析失败, 按游客处理");
        }
        return userId;
    }
}

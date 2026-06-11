package com.foodapp.search.service;

import com.foodapp.search.client.RecipeClient;
import com.foodapp.search.client.UserClient;
import com.foodapp.search.vo.RecommendVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 个性化推荐服务。
 * 推荐策略：
 * 1. 登录用户 → 调用户服务取标签 → 标签映射为菜谱标签 → 逐标签查菜谱聚合去重；
 * 2. 游客 / 无标签用户 → 按热度取菜谱（菜谱服务默认热度排序）；
 * 3. 可用性优先：任一下游标签链路失败，降级为游客策略，绝不抛错。
 */
@Service
public class RecommendService {

    private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

    /** 游客/降级策略的推荐理由文案 */
    private static final String GUEST_REASON = "大家都在做的高热度菜谱";

    /** 每个映射标签向菜谱服务取的候选条数 */
    private static final String PER_TAG_SIZE = "5";

    /**
     * 用户标签 → 菜谱标签 映射表（未命中映射的标签直接按同名匹配）。
     * 业务依据：Z 世代场景标签与菜谱人群标签不同名，需在推荐层做语义转换。
     */
    private static final Map<String, List<String>> TAG_MAPPING = Map.of(
            "熬夜党", List.of("快手菜"),
            "健身后", List.of("高蛋白"),
            "减脂期", List.of("低卡"),
            "生理期", List.of("生理期友好"),
            "增肌期", List.of("高蛋白")
    );

    private final UserClient userClient;
    private final RecipeClient recipeClient;

    /**
     * 构造注入依赖。
     *
     * @param userClient   用户服务客户端（取标签）
     * @param recipeClient 菜谱服务客户端（查菜谱）
     */
    public RecommendService(UserClient userClient, RecipeClient recipeClient) {
        this.userClient = userClient;
        this.recipeClient = recipeClient;
    }

    /**
     * 个性化推荐入口。
     *
     * @param userId        当前登录用户ID（游客为 null）
     * @param authorization 原始请求的 Authorization 头（登录用户取标签时原样转发；游客为 null）
     * @param limit         推荐条数上限
     * @return 推荐结果（reason + RecipeSummary 列表）
     */
    public RecommendVO recommend(Long userId, String authorization, int limit) {
        // 关键判断：推荐策略分支 —— 登录用户优先走标签个性化，失败或无标签回落游客策略
        if (userId != null) {
            try {
                RecommendVO personalized = recommendByUserTags(userId, authorization, limit);
                if (personalized != null) {
                    return personalized;
                }
                log.info("[推荐] 用户无标签, 回落游客策略: userId={}", userId);
            } catch (Exception e) {
                // 关键判断：下游失败降级 —— 可用性优先，标签链路任一环节失败均降级为游客策略，不抛错
                log.warn("[推荐] 推荐降级: 标签个性化链路失败, userId={}, 原因={}", userId, e.getMessage());
            }
        } else {
            log.info("[推荐] 游客访问, 采用热度推荐策略");
        }
        return recommendForGuest(limit);
    }

    /**
     * 登录用户标签个性化推荐：取标签 → 映射 → 逐标签查菜谱 → 按菜谱ID聚合去重 → 截取 limit 条。
     *
     * @param userId        当前登录用户ID
     * @param authorization 原始 Authorization 头（原样转发给用户服务）
     * @param limit         推荐条数上限
     * @return 推荐结果；用户无标签时返回 null（由上层回落游客策略）
     */
    private RecommendVO recommendByUserTags(Long userId, String authorization, int limit) {
        List<Map<String, Object>> tags = userClient.listUserTags(authorization);
        List<String> tagNames = tags.stream()
                .map(t -> t.get("tagName"))
                .filter(n -> n instanceof String s && !s.isBlank())
                .map(Object::toString)
                .toList();
        // 关键判断：用户无任何标签 → 无个性化依据，交由上层回落游客策略
        if (tagNames.isEmpty()) {
            return null;
        }

        // 标签映射：用户场景标签 → 菜谱人群标签（LinkedHashSet 保序去重）
        LinkedHashSet<String> mappedTags = new LinkedHashSet<>();
        for (String tagName : tagNames) {
            mappedTags.addAll(TAG_MAPPING.getOrDefault(tagName, List.of(tagName)));
        }
        log.info("[推荐] 推荐依据: userId={}, 用户标签={}, 映射菜谱标签={}", userId, tagNames, mappedTags);

        // 逐个映射标签查菜谱并按菜谱ID聚合去重（LinkedHashMap 保持标签优先级顺序）
        LinkedHashMap<Long, Map<String, Object>> aggregated = new LinkedHashMap<>();
        for (String mappedTag : mappedTags) {
            Map<String, Object> data = recipeClient.listRecipes(
                    Map.of("page", "1", "size", PER_TAG_SIZE, "tagName", mappedTag));
            for (Map<String, Object> recipe : extractList(data)) {
                if (recipe.get("id") instanceof Number id) {
                    aggregated.putIfAbsent(id.longValue(), recipe);
                }
            }
        }

        List<Map<String, Object>> list = aggregated.values().stream().limit(limit).toList();
        String reason = "根据你的标签【" + String.join("、", tagNames) + "】为你推荐";
        log.info("[推荐] 标签个性化推荐完成: userId={}, 聚合命中={}条, 返回={}条", userId, aggregated.size(), list.size());
        return new RecommendVO(reason, list);
    }

    /**
     * 游客（或降级）推荐策略：取菜谱服务默认热度排序的前 limit 条。
     * 可用性兜底：若热度查询也失败，返回空列表而非报错。
     *
     * @param limit 推荐条数上限
     * @return 推荐结果（reason 固定为高热度文案）
     */
    private RecommendVO recommendForGuest(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            Map<String, Object> data = recipeClient.listRecipes(
                    Map.of("page", "1", "size", String.valueOf(limit)));
            list = extractList(data);
            log.info("[推荐] 游客热度推荐完成: 返回={}条", list.size());
        } catch (Exception e) {
            // 关键判断：兜底降级 —— 热度查询也失败时返回空列表，保证推荐接口永不报错
            log.warn("[推荐] 推荐降级: 热度查询失败, 返回空列表兜底, 原因={}", e.getMessage());
        }
        return new RecommendVO(GUEST_REASON, list);
    }

    /**
     * 从菜谱服务分页结构 data 中提取 list 字段。
     *
     * @param data 分页结构 {total,page,size,list}
     * @return RecipeSummary 列表（结构非法时返回空列表）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractList(Map<String, Object> data) {
        if (data != null && data.get("list") instanceof List<?> list) {
            return new ArrayList<>((List<Map<String, Object>>) list);
        }
        return new ArrayList<>();
    }
}

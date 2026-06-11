package com.foodapp.kitchen.remote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.kitchen.dto.StepDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 菜谱服务远程客户端。
 * 负责调用菜谱服务 GET /api/recipe/{id}（取菜谱名）与 GET /api/recipe/{id}/steps（取步骤平铺列表）。
 * 厨房服务为高频服务，步骤列表在本服务内存中缓存（ConcurrentHashMap，10分钟过期），
 * 降低对菜谱服务的下游压力（性能要求）。
 */
@Component
public class RecipeClient {

    private static final Logger log = LoggerFactory.getLogger(RecipeClient.class);

    /** 步骤缓存过期时间：10分钟 */
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;

    private final RestClient recipeRestClient;
    private final ObjectMapper objectMapper;

    /** 步骤缓存：recipeId -> 缓存条目（步骤列表 + 缓存时间戳） */
    private final ConcurrentHashMap<Long, CacheEntry> stepsCache = new ConcurrentHashMap<>();

    /**
     * 步骤缓存条目。
     *
     * @param steps    步骤平铺列表
     * @param cachedAt 缓存写入时间戳（毫秒）
     */
    private record CacheEntry(List<StepDTO> steps, long cachedAt) {

        /** 判断缓存是否仍然有效（10分钟内） */
        boolean alive() {
            return System.currentTimeMillis() - cachedAt < CACHE_TTL_MS;
        }
    }

    public RecipeClient(RestClient recipeRestClient, ObjectMapper objectMapper) {
        this.recipeRestClient = recipeRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取菜谱名称：调菜谱服务 GET /api/recipe/{id}，解析统一响应体 data.info.title。
     *
     * @param recipeId 菜谱ID
     * @return 菜谱名称
     * @throws BusinessException 菜谱不存在（40400）或下游调用失败（50200）
     */
    public String fetchRecipeName(Long recipeId) {
        JsonNode data = callRecipeService("/api/recipe/" + recipeId, recipeId);
        JsonNode title = data.path("info").path("title");
        // 关键判断：下游返回结构异常（缺少 info.title）视为下游契约不符
        if (title.isMissingNode() || title.isNull()) {
            log.error("[菜谱客户端] 菜谱详情响应缺少 info.title 字段, recipeId={}", recipeId);
            throw new BusinessException(ResultCode.REMOTE_ERROR, "菜谱服务返回数据异常");
        }
        return title.asText();
    }

    /**
     * 获取菜谱步骤平铺列表：优先读内存缓存（10分钟过期），
     * 未命中则调菜谱服务 GET /api/recipe/{id}/steps 并写入缓存。
     *
     * @param recipeId 菜谱ID
     * @return 步骤平铺列表（按阶段 PREPARE→PLATE、阶段内按 stepIndex 排序）
     * @throws BusinessException 菜谱不存在（40400）或下游调用失败（50200）
     */
    public List<StepDTO> fetchSteps(Long recipeId) {
        CacheEntry entry = stepsCache.get(recipeId);
        // 关键判断：缓存命中且未过期，直接返回，不打下游（高频性能优化）
        if (entry != null && entry.alive()) {
            log.debug("[菜谱客户端] 步骤缓存命中, recipeId={}", recipeId);
            return entry.steps();
        }
        JsonNode data = callRecipeService("/api/recipe/" + recipeId + "/steps", recipeId);
        if (!data.isArray()) {
            log.error("[菜谱客户端] 步骤列表响应 data 不是数组, recipeId={}", recipeId);
            throw new BusinessException(ResultCode.REMOTE_ERROR, "菜谱服务返回数据异常");
        }
        List<StepDTO> steps = new ArrayList<>();
        for (JsonNode node : data) {
            steps.add(objectMapper.convertValue(node, StepDTO.class));
        }
        stepsCache.put(recipeId, new CacheEntry(List.copyOf(steps), System.currentTimeMillis()));
        log.info("[菜谱客户端] 已从菜谱服务拉取步骤并缓存10分钟, recipeId={}, 步骤数={}", recipeId, steps.size());
        return steps;
    }

    /**
     * 调用菜谱服务并解析统一响应体，返回 data 节点。
     *
     * @param uri      下游接口路径
     * @param recipeId 菜谱ID（用于日志与错误提示）
     * @return 统一响应体中的 data 节点
     * @throws BusinessException 下游异常时抛出
     */
    private JsonNode callRecipeService(String uri, Long recipeId) {
        JsonNode root;
        try {
            root = recipeRestClient.get().uri(uri).retrieve().body(JsonNode.class);
        } catch (RestClientException e) {
            // 关键判断：下游网络/HTTP 异常，记录 error 日志并抛远程调用失败
            log.error("[菜谱客户端] 调用菜谱服务失败, uri={}, 原因: {}", uri, e.getMessage());
            throw new BusinessException(ResultCode.REMOTE_ERROR);
        }
        if (root == null || root.path("code").isMissingNode()) {
            log.error("[菜谱客户端] 菜谱服务响应为空或缺少 code 字段, uri={}", uri);
            throw new BusinessException(ResultCode.REMOTE_ERROR);
        }
        int code = root.path("code").asInt();
        // 关键判断：下游返回 40400 表示菜谱不存在，向上抛资源不存在而非远程错误
        if (code == ResultCode.NOT_FOUND.getCode()) {
            log.warn("[菜谱客户端] 菜谱不存在, recipeId={}", recipeId);
            throw new BusinessException(ResultCode.NOT_FOUND, "菜谱不存在");
        }
        if (code != ResultCode.SUCCESS.getCode()) {
            log.error("[菜谱客户端] 菜谱服务返回业务失败, uri={}, code={}, message={}",
                    uri, code, root.path("message").asText());
            throw new BusinessException(ResultCode.REMOTE_ERROR);
        }
        return root.path("data");
    }
}

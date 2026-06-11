package com.foodapp.social.service;

import com.foodapp.social.dto.UserPublicVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户公开信息服务。
 * 评论/帖子列表展示昵称头像时，调用用户服务 GET /api/user/public/{id} 获取公开信息，
 * 使用 ConcurrentHashMap 做 10 分钟内存缓存；下游失败时降级为默认昵称"美食家"，
 * 保证社交列表的可用性优先（不因用户服务抖动而拖垮列表接口）。
 */
@Service
public class UserInfoService {

    private static final Logger log = LoggerFactory.getLogger(UserInfoService.class);

    /** 缓存有效期：10 分钟 */
    private static final long CACHE_TTL_MILLIS = 10 * 60 * 1000L;

    /** 下游统一响应体的反序列化类型 */
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient userRestClient;

    /** 用户公开信息内存缓存：userId -> 缓存项（含过期时间戳） */
    private final Map<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 构造时按配置 foodapp.services.user-url 创建指向用户服务的 RestClient。
     *
     * @param userUrl 用户服务地址（dev 默认 http://localhost:8081）
     */
    public UserInfoService(@Value("${foodapp.services.user-url}") String userUrl) {
        log.info("[配置] 用户服务地址: {}", userUrl);
        this.userRestClient = RestClient.builder().baseUrl(userUrl).build();
    }

    /**
     * 获取单个用户的公开信息（昵称/头像），优先走 10 分钟内存缓存。
     * 下游调用失败时降级返回 nickname="美食家"、avatarUrl=null（降级结果不写缓存，便于下游恢复后自动回源）。
     *
     * @param userId 用户ID
     * @return 用户公开信息（永不为 null）
     */
    public UserPublicVO getUser(Long userId) {
        long now = System.currentTimeMillis();
        CacheEntry entry = cache.get(userId);
        // 关键判断：缓存命中且未过期，直接返回
        if (entry != null && entry.expireAt() > now) {
            return entry.vo();
        }
        try {
            Map<String, Object> resp = userRestClient.get()
                    .uri("/api/user/public/{id}", userId)
                    .retrieve()
                    .body(RESPONSE_TYPE);
            // 关键判断：下游业务码非 0 视为调用失败，走降级
            if (resp == null || !(resp.get("code") instanceof Number code) || code.intValue() != 0) {
                log.warn("[用户信息] 用户服务返回异常响应, userId={}, resp={}, 降级为默认昵称", userId, resp);
                return fallback(userId);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            if (data == null) {
                log.warn("[用户信息] 用户服务返回空data, userId={}, 降级为默认昵称", userId);
                return fallback(userId);
            }
            UserPublicVO vo = new UserPublicVO(
                    userId,
                    data.get("nickname") == null ? "美食家" : String.valueOf(data.get("nickname")),
                    data.get("avatarUrl") == null ? null : String.valueOf(data.get("avatarUrl")));
            cache.put(userId, new CacheEntry(vo, now + CACHE_TTL_MILLIS));
            return vo;
        } catch (Exception e) {
            // 关键判断：下游服务不可用，降级返回默认昵称，列表可用性优先
            log.warn("[用户信息] 调用用户服务失败, userId={}, 原因: {}，降级为默认昵称", userId, e.getMessage());
            return fallback(userId);
        }
    }

    /**
     * 批量获取用户公开信息（列表装配场景，内部按缓存逐个回源）。
     *
     * @param userIds 用户ID集合（可含重复）
     * @return userId -> 公开信息 的映射
     */
    public Map<Long, UserPublicVO> getUsers(Collection<Long> userIds) {
        Map<Long, UserPublicVO> result = new HashMap<>();
        for (Long userId : userIds) {
            result.computeIfAbsent(userId, this::getUser);
        }
        return result;
    }

    /**
     * 构造降级的默认公开信息。
     *
     * @param userId 用户ID
     * @return nickname="美食家"、avatarUrl=null 的兜底对象
     */
    private UserPublicVO fallback(Long userId) {
        return new UserPublicVO(userId, "美食家", null);
    }

    /**
     * 缓存项：公开信息 + 过期时间戳（毫秒）。
     */
    private record CacheEntry(UserPublicVO vo, long expireAt) {
    }
}

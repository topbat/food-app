package com.foodapp.search.client;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 用户服务 HTTP 客户端。
 * 个性化推荐时调用 GET /api/user/tag/list 获取当前用户的 Z 世代标签，
 * 调用时原样转发请求方的 Authorization 头（用户服务自行完成鉴权）。
 */
@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    /** 下游统一响应体的反序列化类型 */
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient userRestClient;

    /**
     * 构造注入用户服务 RestClient。
     *
     * @param userRestClient baseUrl 指向用户服务的 RestClient
     */
    public UserClient(@Qualifier("userRestClient") RestClient userRestClient) {
        this.userRestClient = userRestClient;
    }

    /**
     * 查询当前用户的标签列表（GET /api/user/tag/list，原样转发 Authorization 头）。
     *
     * @param authorization 原始请求的 Authorization 头（Bearer Token）
     * @return 标签列表，每项形如 {id,tagName,tagType}
     * @throws BusinessException 下游返回非 0 状态码或响应结构非法时抛出
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listUserTags(String authorization) {
        Map<String, Object> resp = userRestClient.get()
                .uri("/api/user/tag/list")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(RESPONSE_TYPE);
        // 关键判断：下游业务码非 0 视为调用失败（401 等 HTTP 异常由 RestClient 直接抛出）
        if (resp == null || !(resp.get("code") instanceof Number code) || code.intValue() != 0) {
            log.warn("[用户服务] 返回异常响应: {}", resp);
            throw new BusinessException(ResultCode.REMOTE_ERROR, "用户服务返回异常");
        }
        List<Map<String, Object>> tags = (List<Map<String, Object>>) resp.get("data");
        return tags == null ? List.of() : tags;
    }
}

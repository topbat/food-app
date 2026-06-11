package com.foodapp.search.client;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 菜谱服务 HTTP 客户端。
 * 调用菜谱服务 GET /api/recipe/list 做结构化检索；
 * prod 环境如切换 Elasticsearch 全文检索，只需替换本类实现，上层 Service 无感知。
 */
@Component
public class RecipeClient {

    private static final Logger log = LoggerFactory.getLogger(RecipeClient.class);

    /** 下游统一响应体的反序列化类型 */
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient recipeRestClient;

    /**
     * 构造注入菜谱服务 RestClient。
     *
     * @param recipeRestClient baseUrl 指向菜谱服务的 RestClient
     */
    public RecipeClient(@Qualifier("recipeRestClient") RestClient recipeRestClient) {
        this.recipeRestClient = recipeRestClient;
    }

    /**
     * 调用菜谱服务分页查询接口（GET /api/recipe/list），原样透传查询条件。
     *
     * @param params 查询参数（page/size/cuisineType/difficulty/keyword/maxCalories/tagName/ingredients）
     * @return 下游统一响应中的 data（分页结构 {total,page,size,list}）
     * @throws BusinessException 下游返回非 0 状态码或响应结构非法时抛出
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listRecipes(Map<String, String> params) {
        Map<String, Object> resp = recipeRestClient.get()
                .uri(builder -> {
                    builder.path("/api/recipe/list");
                    params.forEach(builder::queryParam);
                    return builder.build();
                })
                .retrieve()
                .body(RESPONSE_TYPE);
        // 关键判断：下游业务码非 0 视为调用失败
        if (resp == null || !(resp.get("code") instanceof Number code) || code.intValue() != 0) {
            log.warn("[菜谱服务] 返回异常响应: {}", resp);
            throw new BusinessException(ResultCode.REMOTE_ERROR, "菜谱服务返回异常");
        }
        return (Map<String, Object>) resp.get("data");
    }
}

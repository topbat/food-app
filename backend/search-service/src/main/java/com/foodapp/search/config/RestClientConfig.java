package com.foodapp.search.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 跨服务 HTTP 客户端配置。
 * 提供两个 RestClient：菜谱服务（结构化检索）与用户服务（个性化推荐取标签），
 * 服务地址按环境通过 foodapp.services.* 配置注入。
 */
@Configuration
public class RestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfig.class);

    /** 菜谱服务地址（dev 默认 http://localhost:8082） */
    @Value("${foodapp.services.recipe-url}")
    private String recipeUrl;

    /** 用户服务地址（dev 默认 http://localhost:8081） */
    @Value("${foodapp.services.user-url}")
    private String userUrl;

    /**
     * 菜谱服务 RestClient：透传搜索条件调用 /api/recipe/list。
     *
     * @return 以菜谱服务地址为 baseUrl 的 RestClient
     */
    @Bean
    public RestClient recipeRestClient() {
        log.info("[配置] 菜谱服务地址: {}", recipeUrl);
        return RestClient.builder().baseUrl(recipeUrl).build();
    }

    /**
     * 用户服务 RestClient：个性化推荐时调用 /api/user/tag/list 获取用户标签。
     *
     * @return 以用户服务地址为 baseUrl 的 RestClient
     */
    @Bean
    public RestClient userRestClient() {
        log.info("[配置] 用户服务地址: {}", userUrl);
        return RestClient.builder().baseUrl(userUrl).build();
    }
}

package com.foodapp.kitchen.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 跨服务 HTTP 客户端配置。
 * 厨房服务创建会话时需要调用菜谱服务获取菜谱名与步骤平铺列表。
 */
@Configuration
public class RestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfig.class);

    /**
     * 菜谱服务 RestClient（baseUrl 取配置 foodapp.services.recipe-url，按环境区分）。
     *
     * @param recipeUrl 菜谱服务基础地址
     * @return RestClient 实例
     */
    @Bean
    public RestClient recipeRestClient(@Value("${foodapp.services.recipe-url}") String recipeUrl) {
        log.info("[配置] 菜谱服务地址: {}", recipeUrl);
        return RestClient.builder().baseUrl(recipeUrl).build();
    }
}

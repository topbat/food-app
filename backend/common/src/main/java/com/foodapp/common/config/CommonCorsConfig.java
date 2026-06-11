package com.foodapp.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 统一跨域配置。
 * 安全要求：仅放行配置中声明的前端来源（按 dev/uat/prod 环境分别配置），
 * 禁止使用 * 通配生产来源。
 */
@Configuration
public class CommonCorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CommonCorsConfig.class);

    /**
     * 允许的前端来源列表（支持通配模式），按环境配置 foodapp.cors.allowed-origins。
     * dev 默认同时放行 localhost 与 127.0.0.1（浏览器把两者视为不同 Origin，
     * 只配 localhost 时用 127.0.0.1 访问会被拒绝为 Invalid CORS request）。
     */
    @Value("${foodapp.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173,http://127.0.0.1:4173}")
    private List<String> allowedOrigins;

    /**
     * 注册 CORS 过滤器：只放行白名单来源，允许携带 Authorization 头。
     * 使用 OriginPatterns 以支持通配（如 http://192.168.*.*:5173 局域网真机调试）。
     */
    @Bean
    public CorsFilter corsFilter() {
        log.info("[CORS] 允许跨域来源: {}", allowedOrigins);
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

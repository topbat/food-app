package com.foodapp.search.config;

import com.foodapp.common.auth.AuthInterceptor;
import com.foodapp.common.auth.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 鉴权拦截器。
 * 本服务大部分接口对游客开放，仅搜索历史接口（/api/search/history**）强制登录；
 * /api/search/recipes 与 /api/search/recommend 为"可选登录"，由 Controller 手动解析 Authorization 头。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    private final JwtUtil jwtUtil;

    /**
     * 构造注入 common 提供的 JwtUtil。
     *
     * @param jwtUtil JWT 工具类（common 模块 Bean）
     */
    public WebMvcConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册鉴权拦截器：只拦截 /api/search/history 及其子路径，其余接口公开（可选登录）。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("[配置] 注册JWT鉴权拦截器，仅拦截 /api/search/history**，其余接口公开（可选登录）");
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/search/history", "/api/search/history/**");
    }
}

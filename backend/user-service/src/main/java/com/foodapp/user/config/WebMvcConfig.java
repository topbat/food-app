package com.foodapp.user.config;

import com.foodapp.common.auth.AuthInterceptor;
import com.foodapp.common.auth.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 鉴权拦截器并配置白名单。
 * 白名单：注册、登录、公开用户信息接口无需登录即可访问，其余 /api/** 一律鉴权。
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
     * 注册鉴权拦截器：拦截 /api/**，放行注册/登录/公开信息接口。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("[配置] 注册JWT鉴权拦截器，白名单：/api/user/register、/api/user/login、/api/user/public/**");
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login",
                        "/api/user/public/**"
                );
    }
}

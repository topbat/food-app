package com.foodapp.kitchen.config;

import com.foodapp.common.auth.AuthInterceptor;
import com.foodapp.common.auth.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 鉴权拦截器。
 * 厨房服务全部接口（/api/kitchen/**）均需登录后访问，无白名单。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;

    public WebMvcConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册鉴权拦截器：拦截 /api/kitchen/** 全部请求。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/kitchen/**");
    }
}

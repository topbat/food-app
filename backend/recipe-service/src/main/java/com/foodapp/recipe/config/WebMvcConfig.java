package com.foodapp.recipe.config;

import com.foodapp.common.auth.AuthInterceptor;
import com.foodapp.common.auth.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 鉴权拦截器。
 * 菜谱服务除 UGC 上传外全部公开，因此只拦截 /api/recipe/ugc/**。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    private final JwtUtil jwtUtil;

    public WebMvcConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册鉴权拦截器：仅拦截 UGC 上传接口，其余菜谱查询接口全部公开访问。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("[配置] 注册JWT鉴权拦截器, 仅拦截 /api/recipe/ugc/**, 其余接口公开");
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/recipe/ugc", "/api/recipe/ugc/**");
    }
}

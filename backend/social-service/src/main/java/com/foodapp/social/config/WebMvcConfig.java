package com.foodapp.social.config;

import com.foodapp.common.auth.AuthInterceptor;
import com.foodapp.common.auth.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 鉴权拦截器并配置白名单。
 * 白名单：评论列表、作品墙列表、帖子详情、评分查询为公开接口；
 * 其余 /api/social/** 写操作（发评论/发帖/点赞/评分/打卡/分享卡等）一律鉴权。
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
     * 注册鉴权拦截器：拦截 /api/social/**，放行公开查询接口。
     * 注意：/api/social/post/* 仅匹配“post 下一级路径段”（即帖子详情 GET /api/social/post/{id}），
     * 不会放行 POST /api/social/post 发帖接口；/api/social/rating/* 同理仅放行评分查询。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("[配置] 注册JWT鉴权拦截器，白名单：/api/social/comment/list、/api/social/post/list、/api/social/post/*、/api/social/rating/*");
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/social/**")
                .excludePathPatterns(
                        "/api/social/comment/list",
                        "/api/social/post/list",
                        "/api/social/post/*",
                        "/api/social/rating/*"
                );
    }
}

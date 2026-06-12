package com.foodapp.file.config;

import com.foodapp.common.auth.AuthInterceptor;
import com.foodapp.common.auth.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 鉴权拦截器。
 * 文件服务全部接口需要登录后访问（上传/查询记录均涉及用户归属），因此不配置白名单。
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
     * 注册鉴权拦截器：拦截全部 /api/**，无白名单（文件服务所有接口均需登录）。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("[配置] 注册JWT鉴权拦截器，文件服务全部接口需鉴权（无白名单）");
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/**");
    }
}

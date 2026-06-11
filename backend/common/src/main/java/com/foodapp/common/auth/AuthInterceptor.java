package com.foodapp.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * JWT 鉴权拦截器。
 * 各微服务在自己的 WebMvcConfigurer 中注册本拦截器并配置白名单（登录、注册、公开查询等）。
 * 校验通过后将 userId 写入 request attribute，业务层通过 UserContext 获取当前登录用户。
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    /** request attribute 中存放当前登录用户ID的键名 */
    public static final String ATTR_USER_ID = "FOODAPP_USER_ID";

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 请求前置拦截：校验 Authorization 头中的 Bearer Token。
     *
     * @return true 放行；false 拦截并返回 401 响应
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        // 关键判断：请求头缺失或格式不对，直接拒绝
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[鉴权] 缺少有效Authorization头, uri={}", request.getRequestURI());
            writeUnauthorized(response);
            return false;
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        // 关键判断：Token 校验失败（过期/伪造）
        if (userId == null) {
            log.warn("[鉴权] Token无效或已过期, uri={}", request.getRequestURI());
            writeUnauthorized(response);
            return false;
        }
        request.setAttribute(ATTR_USER_ID, userId);
        return true;
    }

    /**
     * 输出 401 统一响应体。
     */
    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":40100,\"message\":\"未登录或登录已过期\",\"data\":null}");
    }
}

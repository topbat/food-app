package com.foodapp.common.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 访问日志过滤器。
 * 对每个 HTTP 请求输出「方法 / 路径 / 状态码 / 耗时 / 响应摘要」日志，
 * 满足"后端要对输出进行日志化"的要求；响应体超长时截断，避免日志爆炸。
 */
@Component
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("ACCESS");

    /** 响应体日志最大长度（字符），超出截断 */
    private static final int MAX_BODY_LOG_LENGTH = 500;

    /**
     * 过滤逻辑：包装响应以便读取响应体，请求完成后统一输出访问日志。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            long cost = System.currentTimeMillis() - start;
            String body = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
            // 关键判断：响应体过长则截断，防止大列表响应刷爆日志文件
            if (body.length() > MAX_BODY_LOG_LENGTH) {
                body = body.substring(0, MAX_BODY_LOG_LENGTH) + "...(截断)";
            }
            log.info("[访问日志] {} {} | 状态={} | 耗时={}ms | 响应={}",
                    request.getMethod(), request.getRequestURI(), wrappedResponse.getStatus(), cost, body);
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * 静态资源与健康检查不记录访问日志，降低噪音。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator") || uri.endsWith(".ico");
    }
}

package com.foodapp.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类。
 * 所有微服务共享同一套签名密钥（按环境配置 foodapp.jwt.secret），
 * 用户服务负责签发，其余服务只做校验。算法：HS256。
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /** JWT 签名密钥（按 dev/uat/prod 环境分别配置，禁止硬编码进代码仓库的生产密钥） */
    @Value("${foodapp.jwt.secret:foodapp-dev-secret-key-must-be-at-least-256-bits-long!}")
    private String secret;

    /** Token 有效期（秒），默认 7 天 */
    @Value("${foodapp.jwt.expire-seconds:604800}")
    private long expireSeconds;

    /**
     * 获取签名密钥对象。
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 为指定用户签发 Token。
     *
     * @param userId   用户ID（作为 subject）
     * @param nickname 用户昵称（放入自定义 claim，便于展示）
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String nickname) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireSeconds * 1000);
        log.info("[JWT] 签发Token: userId={}, 过期时间={}", userId, expiry);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("nickname", nickname)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getKey())
                .compact();
    }

    /**
     * 校验并解析 Token。
     *
     * @param token JWT 字符串
     * @return 解析出的 Claims；校验失败（过期/篡改）返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            // 关键判断：Token 非法或已过期，记录告警日志便于排查异常调用
            log.warn("[JWT] Token校验失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中提取用户ID。
     *
     * @param token JWT 字符串
     * @return 用户ID；Token 非法时返回 null
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : Long.valueOf(claims.getSubject());
    }
}

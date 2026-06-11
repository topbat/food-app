package com.foodapp.common.auth;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 当前登录用户上下文工具。
 * 从 AuthInterceptor 写入的 request attribute 中获取 userId，供业务层使用。
 */
public final class UserContext {

    private UserContext() {
    }

    /**
     * 获取当前登录用户ID。
     *
     * @return 用户ID
     * @throws BusinessException 未登录（白名单接口误用本方法）时抛出
     */
    public static Long requireUserId() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * 获取当前登录用户ID（可能为空，用于公开接口中区分游客/登录用户）。
     *
     * @return 用户ID；游客返回 null
     */
    public static Long currentUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        Object userId = request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        return userId == null ? null : (Long) userId;
    }
}

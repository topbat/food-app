package com.foodapp.kitchen.support;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.kitchen.entity.KitchenSession;
import com.foodapp.kitchen.repository.KitchenSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 会话归属守卫。
 * 安全要求：每个会话操作必须校验会话归属当前登录用户，
 * 防止通过遍历会话ID越权操作他人会话（水平越权防护）。
 */
@Component
public class SessionGuard {

    private static final Logger log = LoggerFactory.getLogger(SessionGuard.class);

    private final KitchenSessionRepository sessionRepository;

    public SessionGuard(KitchenSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * 加载会话并校验归属当前登录用户。
     *
     * @param sessionId 会话ID
     * @return 归属校验通过的会话实体
     * @throws BusinessException 会话不存在（40400）或非本人会话（40300）
     */
    public KitchenSession loadOwnedSession(Long sessionId) {
        Long userId = UserContext.requireUserId();
        KitchenSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "烹饪会话不存在"));
        // 关键判断：会话归属校验，非本人会话一律拒绝（防越权，安全要求）
        if (!session.getUserId().equals(userId)) {
            log.warn("[会话守卫] 用户{}尝试操作他人会话{}（归属用户{}），已拒绝", userId, sessionId, session.getUserId());
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作他人的烹饪会话");
        }
        return session;
    }
}

package com.foodapp.user.repository;

import com.foodapp.user.entity.UserHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户健康档案数据访问层。
 */
public interface UserHealthProfileRepository extends JpaRepository<UserHealthProfile, Long> {

    /**
     * 按用户ID查询健康档案（一人一档）。
     *
     * @param userId 用户ID
     * @return 健康档案（可能为空）
     */
    Optional<UserHealthProfile> findByUserId(Long userId);
}

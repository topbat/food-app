package com.foodapp.user.repository;

import com.foodapp.user.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户账号数据访问层。
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * 按用户名查询账号（登录、注册查重使用）。
     *
     * @param username 登录用户名
     * @return 账号实体（可能为空）
     */
    Optional<UserAccount> findByUsername(String username);

    /**
     * 判断用户名是否已存在（注册查重使用）。
     *
     * @param username 登录用户名
     * @return true 已存在
     */
    boolean existsByUsername(String username);
}

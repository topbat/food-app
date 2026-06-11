package com.foodapp.user.repository;

import com.foodapp.user.entity.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 用户标签数据访问层。
 */
public interface UserTagRepository extends JpaRepository<UserTag, Long> {

    /**
     * 查询某用户的全部标签（按ID升序）。
     *
     * @param userId 用户ID
     * @return 标签列表
     */
    List<UserTag> findByUserIdOrderByIdAsc(Long userId);

    /**
     * 按标签ID + 用户ID查询（删除前校验归属，防止越权删除他人标签）。
     *
     * @param id     标签ID
     * @param userId 用户ID
     * @return 标签（可能为空）
     */
    Optional<UserTag> findByIdAndUserId(Long id, Long userId);

    /**
     * 判断某用户是否已拥有同名标签（防重复添加）。
     *
     * @param userId  用户ID
     * @param tagName 标签名称
     * @return true 已存在
     */
    boolean existsByUserIdAndTagName(Long userId, String tagName);
}

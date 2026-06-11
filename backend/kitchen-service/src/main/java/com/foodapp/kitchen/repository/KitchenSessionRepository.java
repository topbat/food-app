package com.foodapp.kitchen.repository;

import com.foodapp.kitchen.entity.KitchenSession;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 烹饪会话仓储。
 */
public interface KitchenSessionRepository extends JpaRepository<KitchenSession, Long> {
}

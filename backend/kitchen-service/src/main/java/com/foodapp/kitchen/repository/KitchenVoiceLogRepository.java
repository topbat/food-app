package com.foodapp.kitchen.repository;

import com.foodapp.kitchen.entity.KitchenVoiceLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 语音指令解析日志仓储。
 */
public interface KitchenVoiceLogRepository extends JpaRepository<KitchenVoiceLog, Long> {
}

-- =============================================
-- 智能搜索推荐服务（search-service :8084）表结构
-- 表前缀：search_
-- =============================================
USE `food_app`;

-- 搜索历史表
CREATE TABLE IF NOT EXISTS `search_history` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     BIGINT       DEFAULT NULL COMMENT '关联用户ID（游客为空）',
  `keyword`     VARCHAR(100) NOT NULL COMMENT '搜索关键词',
  `search_type` TINYINT      NOT NULL DEFAULT 1 COMMENT '搜索类型（1关键词 2按食材/冰箱清理 3按营养目标 4按人群）',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史表';

-- 热搜关键词表
CREATE TABLE IF NOT EXISTS `search_hot_keyword` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `keyword`      VARCHAR(100) NOT NULL COMMENT '热搜关键词',
  `search_count` BIGINT       NOT NULL DEFAULT 0 COMMENT '累计搜索次数（用于热度排序）',
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热搜关键词表';

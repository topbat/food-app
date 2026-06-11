-- =============================================
-- 厨房引擎服务（kitchen-service :8083）表结构
-- 表前缀：kitchen_
-- =============================================
USE `food_app`;

-- 烹饪会话表（厨房模式状态机）
CREATE TABLE IF NOT EXISTS `kitchen_session` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`            BIGINT       NOT NULL COMMENT '关联用户ID',
  `recipe_id`          BIGINT       NOT NULL COMMENT '关联菜谱ID',
  `recipe_name`        VARCHAR(100) NOT NULL COMMENT '菜谱名称（冗余存储便于展示）',
  `current_phase`      VARCHAR(10)  NOT NULL DEFAULT 'PREPARE' COMMENT '当前阶段（PREPARE/WASH/CUT/COOK/PLATE，状态不可逆跳跃）',
  `current_step_index` INT          NOT NULL DEFAULT 1 COMMENT '当前阶段内步骤序号',
  `status`             TINYINT      NOT NULL DEFAULT 1 COMMENT '会话状态（1进行中 2已完成 3已放弃）',
  `started_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始烹饪时间',
  `finished_at`        DATETIME     DEFAULT NULL COMMENT '完成时间',
  `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='烹饪会话表（厨房模式状态机）';

-- 烹饪倒计时表（支持多任务并行计时）
CREATE TABLE IF NOT EXISTS `kitchen_timer` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id`      BIGINT      NOT NULL COMMENT '关联烹饪会话ID',
  `timer_name`      VARCHAR(50) NOT NULL COMMENT '计时器名称（如：焯水、焖煮）',
  `total_sec`       INT         NOT NULL COMMENT '总计时秒数',
  `status`          TINYINT     NOT NULL DEFAULT 1 COMMENT '计时状态（1计时中 2已完成 3已取消）',
  `started_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始计时时间',
  `expected_end_at` DATETIME    NOT NULL COMMENT '预计结束时间（到点推送完成事件）',
  `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_status` (`session_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='烹饪倒计时表（多任务并行计时）';

-- 语音指令解析日志表
CREATE TABLE IF NOT EXISTS `kitchen_voice_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id`    BIGINT       NOT NULL COMMENT '关联烹饪会话ID',
  `command_text`  VARCHAR(200) NOT NULL COMMENT '用户语音指令原文（如：下一步）',
  `parsed_action` VARCHAR(50)  NOT NULL COMMENT '解析出的动作（NEXT_STEP/PREV_STEP/QUERY_TIMER/START_TIMER/UNKNOWN）',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语音指令解析日志表';

-- =============================================
-- 用户与画像服务（user-service :8081）表结构
-- 表前缀：user_
-- =============================================
USE `food_app`;

-- 用户账号表
CREATE TABLE IF NOT EXISTS `user_account` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名（唯一）',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密存储，禁止明文）',
  `nickname`    VARCHAR(50)  NOT NULL COMMENT '昵称',
  `avatar_url`  VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `gender`      TINYINT      DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
  `birth_date`  DATE         DEFAULT NULL COMMENT '出生日期',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态（0禁用 1正常）',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';

-- 用户健康档案表
CREATE TABLE IF NOT EXISTS `user_health_profile` (
  `id`                   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`              BIGINT        NOT NULL COMMENT '关联用户ID',
  `height_cm`            DECIMAL(5,1)  DEFAULT NULL COMMENT '身高（厘米）',
  `weight_kg`            DECIMAL(5,1)  DEFAULT NULL COMMENT '体重（千克）',
  `allergy_history`      VARCHAR(500)  DEFAULT NULL COMMENT '过敏史（如：海鲜、花生，逗号分隔）',
  `diet_preference`      VARCHAR(200)  DEFAULT NULL COMMENT '饮食偏好（如：少辣、素食）',
  `health_goal`          VARCHAR(20)   DEFAULT NULL COMMENT '健康目标（减脂/增肌/控糖/均衡）',
  `daily_calorie_target` INT           DEFAULT NULL COMMENT '每日摄入热量目标（kcal）',
  `created_at`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户健康档案表';

-- 用户标签表（Z世代标签）
CREATE TABLE IF NOT EXISTS `user_tag` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`    BIGINT      NOT NULL COMMENT '关联用户ID',
  `tag_name`   VARCHAR(30) NOT NULL COMMENT '标签名称（如：熬夜党、健身狂、生理期）',
  `tag_type`   TINYINT     NOT NULL DEFAULT 1 COMMENT '标签类型（1人群标签 2状态标签）',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签表（Z世代画像标签）';

-- 饮食日历记录表（每日能量摄入统计）
CREATE TABLE IF NOT EXISTS `user_diet_record` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       BIGINT       NOT NULL COMMENT '关联用户ID',
  `record_date`   DATE         NOT NULL COMMENT '记录日期',
  `meal_type`     TINYINT      NOT NULL COMMENT '餐次（1早餐 2午餐 3晚餐 4加餐）',
  `recipe_id`     BIGINT       DEFAULT NULL COMMENT '关联菜谱ID（可空，支持手动记录）',
  `recipe_name`   VARCHAR(100) NOT NULL COMMENT '菜品名称（冗余存储便于展示）',
  `calories_kcal` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT '摄入热量（kcal）',
  `carbs_g`       DECIMAL(6,2) DEFAULT 0 COMMENT '碳水化合物（克）',
  `protein_g`     DECIMAL(6,2) DEFAULT 0 COMMENT '蛋白质（克）',
  `fat_g`         DECIMAL(6,2) DEFAULT 0 COMMENT '脂肪（克）',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食日历记录表（每日能量摄入统计）';

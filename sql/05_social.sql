-- =============================================
-- 社交与UGC服务（social-service :8085）表结构
-- 表前缀：social_
-- =============================================
USE `food_app`;

-- 社区帖子表（作品晒图/打卡/美食日记）
CREATE TABLE IF NOT EXISTS `social_post` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       BIGINT        NOT NULL COMMENT '发布用户ID',
  `recipe_id`     BIGINT        DEFAULT NULL COMMENT '关联菜谱ID（晒装盘时关联）',
  `content`       VARCHAR(1000) DEFAULT NULL COMMENT '帖子文字内容',
  `image_urls`    TEXT          COMMENT '图片URL列表（JSON数组字符串）',
  `post_type`     TINYINT       NOT NULL DEFAULT 1 COMMENT '帖子类型（1作品晒图 2烹饪打卡 3美食日记）',
  `like_count`    INT           NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT           NOT NULL DEFAULT 0 COMMENT '评论数',
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '状态（0已删除 1正常）',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_recipe_id` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表（作品晒图/打卡/美食日记）';

-- 评论表（支持针对菜谱整体或某一步骤的评论）
CREATE TABLE IF NOT EXISTS `social_comment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `target_type` TINYINT      NOT NULL DEFAULT 1 COMMENT '评论对象类型（1菜谱 2社区帖子）',
  `target_id`   BIGINT       NOT NULL COMMENT '评论对象ID（菜谱ID或帖子ID）',
  `step_id`     BIGINT       DEFAULT NULL COMMENT '关联步骤ID（可空，针对某一步踩坑的反馈）',
  `user_id`     BIGINT       NOT NULL COMMENT '评论用户ID',
  `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
  `parent_id`   BIGINT       DEFAULT NULL COMMENT '父评论ID（可空，支持楼中楼回复）',
  `like_count`  INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0已删除 1正常）',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表（支持菜谱步骤级评论）';

-- 菜谱评分表
CREATE TABLE IF NOT EXISTS `social_rating` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`    BIGINT   NOT NULL COMMENT '评分用户ID',
  `recipe_id`  BIGINT   NOT NULL COMMENT '关联菜谱ID',
  `score`      TINYINT  NOT NULL COMMENT '评分（1-5星）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_recipe` (`user_id`, `recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱评分表';

-- 烹饪打卡表（连续打卡统计）
CREATE TABLE IF NOT EXISTS `social_checkin` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`         BIGINT       NOT NULL COMMENT '打卡用户ID',
  `checkin_date`    DATE         NOT NULL COMMENT '打卡日期',
  `recipe_id`       BIGINT       DEFAULT NULL COMMENT '关联菜谱ID（可空）',
  `note`            VARCHAR(200) DEFAULT NULL COMMENT '打卡备注',
  `continuous_days` INT          NOT NULL DEFAULT 1 COMMENT '当前连续打卡天数（写入时计算）',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='烹饪打卡表（连续打卡统计）';

-- 成就徽章字典表
CREATE TABLE IF NOT EXISTS `social_badge` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `badge_name`      VARCHAR(50)  NOT NULL COMMENT '徽章名称（如：川菜小当家）',
  `badge_desc`      VARCHAR(200) DEFAULT NULL COMMENT '徽章描述与获得条件说明',
  `icon`            VARCHAR(10)  DEFAULT NULL COMMENT '徽章图标（emoji或图片URL）',
  `condition_type`  VARCHAR(30)  NOT NULL COMMENT '达成条件类型（CHECKIN_DAYS连续打卡/POST_COUNT发帖数/COOK_COUNT烹饪次数）',
  `condition_value` INT          NOT NULL COMMENT '达成条件数值（如连续打卡7天）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就徽章字典表';

-- 用户已获徽章表
CREATE TABLE IF NOT EXISTS `social_user_badge` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `badge_id`    BIGINT   NOT NULL COMMENT '徽章ID',
  `obtained_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_badge` (`user_id`, `badge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户已获徽章表';

-- 点赞记录表（防重复点赞）
CREATE TABLE IF NOT EXISTS `social_like_record` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     BIGINT   NOT NULL COMMENT '点赞用户ID',
  `target_type` TINYINT  NOT NULL COMMENT '点赞对象类型（1帖子 2评论 3菜谱）',
  `target_id`   BIGINT   NOT NULL COMMENT '点赞对象ID',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表（唯一约束防重复点赞）';

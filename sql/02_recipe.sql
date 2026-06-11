-- =============================================
-- 菜谱结构化服务（recipe-service :8082）表结构
-- 表前缀：recipe_
-- =============================================
USE `food_app`;

-- 菜谱主表
CREATE TABLE IF NOT EXISTS `recipe_info` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title`          VARCHAR(100) NOT NULL COMMENT '菜谱名称（如：宫保鸡丁）',
  `cover_url`      VARCHAR(255) DEFAULT NULL COMMENT '封面图/视频URL',
  `cuisine_type`   TINYINT      NOT NULL DEFAULT 9 COMMENT '菜系（1川 2鲁 3粤 4苏 5闽 6浙 7湘 8徽 9家常）',
  `difficulty`     TINYINT      NOT NULL DEFAULT 1 COMMENT '难度（1入门 2进阶 3大厨）',
  `total_time_min` INT          NOT NULL DEFAULT 0 COMMENT '总耗时（分钟）',
  `servings`       INT          NOT NULL DEFAULT 1 COMMENT '份数（营养数据按单人份计算）',
  `calories_kcal`  DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT '单人份热量（kcal，由食材用量动态计算）',
  `carbs_g`        DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT '单人份碳水化合物（克）',
  `protein_g`      DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT '单人份蛋白质（克）',
  `fat_g`          DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT '单人份脂肪（克）',
  `description`    VARCHAR(500) DEFAULT NULL COMMENT '菜谱简介',
  `tips`           VARCHAR(500) DEFAULT NULL COMMENT '小贴士（如：辣椒去籽防辣手）',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0下架 1上架 2待审核）',
  `view_count`     INT          NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count`     INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  `author_id`      BIGINT       DEFAULT NULL COMMENT '作者用户ID（UGC菜谱）',
  `source_type`    TINYINT      NOT NULL DEFAULT 1 COMMENT '来源（1官方 2用户UGC）',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_cuisine_status` (`cuisine_type`, `status`),
  KEY `idx_calories` (`calories_kcal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱主表';

-- 菜谱结构化步骤表（五步法：准备/洗/切/煮/装盘）
CREATE TABLE IF NOT EXISTS `recipe_step` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recipe_id`    BIGINT       NOT NULL COMMENT '关联菜谱ID',
  `phase`        VARCHAR(10)  NOT NULL COMMENT '阶段（PREPARE前期准备 WASH洗 CUT切 COOK煮 PLATE装盘）',
  `step_index`   INT          NOT NULL DEFAULT 1 COMMENT '阶段内排序序号（从1开始）',
  `action_title` VARCHAR(50)  NOT NULL COMMENT '动作标题（如：切丁、热锅倒油）',
  `detail`       TEXT         COMMENT '详细图文描述',
  `media_url`    VARCHAR(255) DEFAULT NULL COMMENT '动图/短视频URL（竖屏短视频或GIF）',
  `timer_sec`    INT          NOT NULL DEFAULT 0 COMMENT '计时秒数（0表示该步骤无需计时）',
  `fire_power`   VARCHAR(20)  DEFAULT NULL COMMENT '火候（大火/中火/小火，仅COOK阶段）',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recipe_phase` (`recipe_id`, `phase`, `step_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱结构化步骤表（五步法）';

-- 基础食材库表（含每100克营养数据）
CREATE TABLE IF NOT EXISTS `recipe_ingredient_lib` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`               VARCHAR(50)  NOT NULL COMMENT '食材名称（如：鸡胸肉）',
  `category`           VARCHAR(20)  DEFAULT NULL COMMENT '食材分类（肉类/蔬菜/调料/主食等）',
  `calories_per_100g`  DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT '每100克热量（kcal）',
  `carbs_per_100g`     DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT '每100克碳水化合物（克）',
  `protein_per_100g`   DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT '每100克蛋白质（克）',
  `fat_per_100g`       DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT '每100克脂肪（克）',
  `taboo_note`         VARCHAR(200) DEFAULT NULL COMMENT '禁忌提示（如：嘌呤较高，痛风人群慎食）',
  `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基础食材库表（含营养成分）';

-- 菜谱食材关联表
CREATE TABLE IF NOT EXISTS `recipe_ingredient` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recipe_id`       BIGINT       NOT NULL COMMENT '关联菜谱ID',
  `ingredient_id`   BIGINT       NOT NULL COMMENT '关联基础食材库ID',
  `ingredient_name` VARCHAR(50)  NOT NULL COMMENT '食材名称（冗余存储便于展示）',
  `amount`          DECIMAL(6,2) NOT NULL COMMENT '用量数值',
  `unit`            VARCHAR(10)  NOT NULL DEFAULT '克' COMMENT '单位（克/毫升/个/勺）',
  `is_essential`    TINYINT      NOT NULL DEFAULT 1 COMMENT '是否核心食材（0可选替换 1必须）',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recipe_id` (`recipe_id`),
  KEY `idx_ingredient_id` (`ingredient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱食材关联表';

-- 标签字典表
CREATE TABLE IF NOT EXISTS `recipe_tag` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tag_name`   VARCHAR(30) NOT NULL COMMENT '标签名称（如：减脂期、素食、快手菜、熬夜党）',
  `tag_type`   TINYINT     NOT NULL DEFAULT 1 COMMENT '标签类型（1人群 2功效 3场景）',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签字典表';

-- 菜谱标签关联表（适宜/慎用人群与功效场景）
CREATE TABLE IF NOT EXISTS `recipe_tag_relation` (
  `id`            BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recipe_id`     BIGINT  NOT NULL COMMENT '关联菜谱ID',
  `tag_id`        BIGINT  NOT NULL COMMENT '关联标签ID',
  `relation_type` TINYINT NOT NULL DEFAULT 1 COMMENT '关系类型（1适宜 2慎用/禁忌）',
  PRIMARY KEY (`id`),
  KEY `idx_recipe_id` (`recipe_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱标签关联表（含适宜与慎用区分）';

-- 智能食材替换规则表
CREATE TABLE IF NOT EXISTS `recipe_substitute_rule` (
  `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `source_ingredient_id` BIGINT       NOT NULL COMMENT '原食材ID（如：五花肉）',
  `target_ingredient_id` BIGINT       NOT NULL COMMENT '替换为食材ID（如：鸡胸肉）',
  `scene`                VARCHAR(20)  NOT NULL COMMENT '适用场景（减脂/控糖/素食/低嘌呤）',
  `reason`               VARCHAR(200) DEFAULT NULL COMMENT '替换理由说明',
  `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_scene` (`source_ingredient_id`, `scene`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能食材替换规则表';

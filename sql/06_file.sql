-- =============================================
-- 文件存储服务（file-service :8087）表结构
-- 表前缀：file_
-- =============================================
USE `food_app`;

-- 文件上传记录表
CREATE TABLE IF NOT EXISTS `file_record` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       BIGINT       NOT NULL COMMENT '上传者用户ID',
  `biz_type`      VARCHAR(20)  NOT NULL DEFAULT 'common' COMMENT '业务类型（avatar头像 recipe菜谱 post帖子 step步骤 common通用）',
  `original_name` VARCHAR(255) DEFAULT NULL COMMENT '原文件名',
  `object_key`    VARCHAR(255) NOT NULL COMMENT '对象键（命名规则：{bizType}/{yyyy/MM/dd}/{uuid}.{ext}）',
  `url`           VARCHAR(500) NOT NULL COMMENT '文件访问地址',
  `thumb_url`     VARCHAR(500) DEFAULT NULL COMMENT '缩略图访问地址（同路径 {uuid}_thumb.jpg；生成失败为NULL）',
  `content_type`  VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型（如 image/png、video/mp4）',
  `size_bytes`    BIGINT       DEFAULT NULL COMMENT '文件大小（字节）',
  `storage_type`  VARCHAR(20)  DEFAULT NULL COMMENT '存储类型（minio/aliyun）',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_file_user_id` (`user_id`),
  KEY `idx_file_biz_type` (`biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传记录表';

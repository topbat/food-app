-- =============================================
-- 菜谱助手「食研社」数据库初始化脚本
-- 适用环境：uat / prod（MySQL 8.0）
-- dev 环境使用 H2 内存库由 JPA 自动建表，无需执行本脚本
-- =============================================
CREATE DATABASE IF NOT EXISTS `food_app`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `food_app`;

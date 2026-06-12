-- =====================================================================
-- 食研社 search-service dev 种子数据（H2 MySQL 模式，启动自动执行）
-- 热搜关键词：覆盖菜名/食材/人群标签三类常见搜索词，供首页热搜 chips 展示
-- =====================================================================
INSERT INTO search_hot_keyword (id, keyword, search_count, updated_at) VALUES
(1,  '红烧肉',   286, CURRENT_TIMESTAMP),
(2,  '可乐鸡翅', 254, CURRENT_TIMESTAMP),
(3,  '酸菜鱼',   231, CURRENT_TIMESTAMP),
(4,  '番茄炒蛋', 198, CURRENT_TIMESTAMP),
(5,  '减脂',     175, CURRENT_TIMESTAMP),
(6,  '快手菜',   162, CURRENT_TIMESTAMP),
(7,  '鸡胸肉',   149, CURRENT_TIMESTAMP),
(8,  '麻婆豆腐', 137, CURRENT_TIMESTAMP),
(9,  '清蒸鲈鱼', 118, CURRENT_TIMESTAMP),
(10, '下饭菜',   104, CURRENT_TIMESTAMP);

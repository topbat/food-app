-- 成就徽章字典种子数据（dev 环境启动时自动执行；H2 内存库每次启动为空库，可直接 INSERT）
-- condition_type：CHECKIN_DAYS 连续打卡天数 / POST_COUNT 累计发帖数
INSERT INTO social_badge (id, badge_name, badge_desc, icon, condition_type, condition_value) VALUES
(1, '首灶之喜', '完成第1次烹饪打卡，迈出下厨第一步', '🔥', 'CHECKIN_DAYS', 1),
(2, '七日烟火', '连续打卡7天，烟火气已成习惯', '🏮', 'CHECKIN_DAYS', 7),
(3, '灶台常客', '连续打卡21天，灶台离不开你了', '🍲', 'CHECKIN_DAYS', 21),
(4, '晒盘新秀', '发布第1篇作品，秀出你的第一盘', '📸', 'POST_COUNT', 1),
(5, '发帖达人', '累计发布5篇作品，分享欲拉满', '✨', 'POST_COUNT', 5),
(6, '人间烟火家', '累计发布20篇作品，社区的人间烟火担当', '🏆', 'POST_COUNT', 20);

-- =====================================================================
-- 食研社 social-service dev 种子数据（H2 MySQL 模式，启动自动执行）
-- 用户 1=demo 美食家小研；2-7 为社区演示用户（user-service DevDataInitializer 创建）
-- 图片素材：/images/recipes/**（成品图）与 /images/steps/**（过程图），与帖子内容对应
-- 帖子类型对齐前端：1晒作品 2美食日记 3经验分享
-- =====================================================================

-- ---------------------------------------------------------------
-- 1. 成就徽章字典 social_badge
-- condition_type：CHECKIN_DAYS 连续打卡天数 / POST_COUNT 累计发帖数
-- ---------------------------------------------------------------
INSERT INTO social_badge (id, badge_name, badge_desc, icon, condition_type, condition_value) VALUES
(1, '首灶之喜', '完成第1次烹饪打卡，迈出下厨第一步', '🔥', 'CHECKIN_DAYS', 1),
(2, '七日烟火', '连续打卡7天，烟火气已成习惯', '🏮', 'CHECKIN_DAYS', 7),
(3, '灶台常客', '连续打卡21天，灶台离不开你了', '🍲', 'CHECKIN_DAYS', 21),
(4, '晒盘新秀', '发布第1篇作品，秀出你的第一盘', '📸', 'POST_COUNT', 1),
(5, '发帖达人', '累计发布5篇作品，分享欲拉满', '✨', 'POST_COUNT', 5),
(6, '人间烟火家', '累计发布20篇作品，社区的人间烟火担当', '🏆', 'POST_COUNT', 20);

-- ---------------------------------------------------------------
-- 2. 社区帖子 social_post（14 条；comment_count 与下方评论条数一致）
-- ---------------------------------------------------------------
INSERT INTO social_post (id, user_id, recipe_id, content, image_urls, post_type, like_count, comment_count, status, created_at, updated_at) VALUES
(1, 2, 11, '人生第一次炒糖色就成功了！全程跟着步骤计时，冰糖熬到琥珀色立刻下肉，一点都不苦。孙子连吃三块说比饭店的还好吃，老婆子我骄傲得很。',
 '["/images/recipes/hongshao-rou.jpg"]', 1, 86, 4, 1, DATEADD(HOUR, -5, CURRENT_TIMESTAMP), DATEADD(HOUR, -5, CURRENT_TIMESTAMP)),
(2, 3, 15, '深夜放毒！酸菜鱼一整盆端上桌，酸菜煸得够久所以汤特别香，鱼片嫩到筷子都夹不稳。最后那勺热油浇下去的滋啦声，就是今晚最治愈的声音。',
 '["/images/recipes/suancai-yu.jpg","/images/steps/phase-cook-1.jpg"]', 1, 132, 4, 1, DATEADD(HOUR, -9, CURRENT_TIMESTAMP), DATEADD(HOUR, -9, CURRENT_TIMESTAMP)),
(3, 4, 19, '减脂第43天打卡。蒜蓉西兰花真的常吃不腻，焯水时加几滴油这招学到了，绿得发亮。配上水煮蛋和半碗杂粮饭，这一餐不到400大卡。',
 '["/images/recipes/suanrong-xilanhua.jpg"]', 1, 58, 2, 1, DATEADD(HOUR, -22, CURRENT_TIMESTAMP), DATEADD(HOUR, -22, CURRENT_TIMESTAMP)),
(4, 5, 14, '回锅肉的灵魂是灯盏窝！肉片煸到边缘卷起来那一刻成就感拉满。蒜苗秆先下叶后下，火候刚刚好，下了两碗米饭，减脂明天再说。',
 '["/images/recipes/huiguo-rou.jpg","/images/steps/phase-cook-2.jpg"]', 1, 95, 3, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP), DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(5, 6, 18, '室友说想吃可乐鸡翅，第一次做就被夸爆！划刀腌制真的有用，特别入味，收汁的时候满屋子都是焦糖香。零失败菜谱推荐给所有厨房新手～',
 '["/images/recipes/kele-jichi.jpg"]', 1, 121, 3, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP), DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(6, 7, 17, '周末买到一条活蹦乱跳的鲈鱼，八分钟大火足汽，关火再虚蒸两分钟，鱼肉刚好离骨。淋豉油浇热油，姜葱香一下就起来了。蒸鱼，时间就是一切。',
 '["/images/recipes/qingzheng-luyu.jpg"]', 1, 74, 2, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP), DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(7, 2, 12, '今天的晚饭日记：一碗番茄炒蛋，一碗白米饭。番茄烫了皮再炒，沙沙的全是汁。人老了就好这一口简单的，红黄一拌，胃和心都暖了。',
 '["/images/recipes/fanqie-chaodan.jpg"]', 2, 67, 2, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP), DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(8, 5, 16, '一人食日记：扬州炒饭。隔夜饭提前打散，大火快翻两分钟，米粒真的会在锅里跳。虾仁火腿豌豆一样不少，一个人也要吃得像模像样。',
 '["/images/recipes/yangzhou-chaofan.jpg"]', 2, 89, 2, 1, DATEADD(DAY, -3, CURRENT_TIMESTAMP), DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(9, 3, 8, '湘菜魂！剁椒鱼头开边铺满剁椒，大火蒸12分钟关火焖3分钟，鱼脑都是嫩的。最后下一把面条拌盘底的汤汁，这才是这道菜的正确收尾方式！',
 '["/images/recipes/duojiao-yutou.jpg"]', 1, 108, 3, 1, DATEADD(DAY, -3, CURRENT_TIMESTAMP), DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(10, 6, 10, '健身房出来20分钟搞定的一餐：鸡胸撕成丝拌时蔬，油醋汁现拌现吃。教练看了我的饮食记录说继续保持，今天也是元气满满的一天！',
 '["/images/recipes/jixiong-shala.jpg"]', 2, 53, 2, 1, DATEADD(DAY, -4, CURRENT_TIMESTAMP), DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(11, 4, NULL, '给新手的切配心得：1）刀和砧板先稳住，手指弯成猫爪扣住食材；2）切丝先切薄片再码齐细切，粗细一致受热才均匀；3）切完葱姜蒜用盐搓手去味。基本功练好，炒菜成功一半。',
 '["/images/steps/phase-cut.jpg"]', 3, 142, 3, 1, DATEADD(DAY, -5, CURRENT_TIMESTAMP), DATEADD(DAY, -5, CURRENT_TIMESTAMP)),
(12, 7, 2, '麻婆豆腐配豆腐有讲究：嫩豆腐口感好但易碎，盐水焯一分钟就稳了。花椒粉一定要起锅再撒，麻香才冲。今晚这碗，麻辣烫香酥嫩鲜活，八字全占。',
 '["/images/recipes/mapo-doufu.jpg"]', 1, 97, 2, 1, DATEADD(DAY, -6, CURRENT_TIMESTAMP), DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(13, 5, NULL, '炒糖色避坑帖：①冷锅冷油下冰糖，全程最小火；②颜色到琥珀色冒鱼眼泡，立刻下料，迟三秒就发苦；③怕溅油就离火再下肉。失败三次总结的血泪经验，希望你们一次成功。',
 '["/images/steps/phase-cook-3.jpg"]', 3, 176, 4, 1, DATEADD(DAY, -7, CURRENT_TIMESTAMP), DATEADD(DAY, -7, CURRENT_TIMESTAMP)),
(14, 2, 1, '宫保鸡丁交作业！碗汁提前调好这步太关键了，大火90秒收汁，鸡丁嫩花生脆。荔枝口的酸甜里带着糊辣香，和饭店一个味儿。',
 '["/images/recipes/gongbao-jiding.jpg"]', 1, 81, 2, 1, DATEADD(DAY, -8, CURRENT_TIMESTAMP), DATEADD(DAY, -8, CURRENT_TIMESTAMP));

-- ---------------------------------------------------------------
-- 3. 评论 social_comment（target_type：1菜谱 2帖子）
-- 帖子评论 38 条（与各帖 comment_count 一致）+ 菜谱评论 10 条
-- ---------------------------------------------------------------
-- 帖子1（红烧肉）4 条
INSERT INTO social_comment (id, target_type, target_id, step_id, user_id, content, parent_id, like_count, status, created_at) VALUES
(1, 2, 1, NULL, 3, '婆婆这色泽绝了，比我点的外卖强一百倍', NULL, 12, 1, DATEADD(HOUR, -4, CURRENT_TIMESTAMP)),
(2, 2, 1, NULL, 5, '糖色火候满分！下次试试加两枚卤蛋一起焖', NULL, 8, 1, DATEADD(HOUR, -4, CURRENT_TIMESTAMP)),
(3, 2, 1, NULL, 6, '已收藏，周末就照着做', NULL, 3, 1, DATEADD(HOUR, -3, CURRENT_TIMESTAMP)),
(4, 2, 1, NULL, 2, '回复楼上：加了的，照片右上角就是，吸饱汤汁特别香', 2, 6, 1, DATEADD(HOUR, -2, CURRENT_TIMESTAMP)),
-- 帖子2（酸菜鱼）4 条
(5, 2, 2, NULL, 4, '大半夜的看到这个，罪过罪过，明天就去买鱼', NULL, 15, 1, DATEADD(HOUR, -8, CURRENT_TIMESTAMP)),
(6, 2, 2, NULL, 7, '鱼片切得很专业啊，蝴蝶片厚薄均匀', NULL, 9, 1, DATEADD(HOUR, -7, CURRENT_TIMESTAMP)),
(7, 2, 2, NULL, 5, '酸菜先煸这步真的不能省，香气完全不一样', NULL, 7, 1, DATEADD(HOUR, -6, CURRENT_TIMESTAMP)),
(8, 2, 2, NULL, 6, '想问问鱼骨熬汤要不要先煎？', NULL, 2, 1, DATEADD(HOUR, -5, CURRENT_TIMESTAMP)),
-- 帖子3（蒜蓉西兰花）2 条
(9, 2, 3, NULL, 6, '43天！太自律了，跟着姐妹一起冲', NULL, 6, 1, DATEADD(HOUR, -20, CURRENT_TIMESTAMP)),
(10, 2, 3, NULL, 2, '焯水加油这招我也用，绿得才好看', NULL, 4, 1, DATEADD(HOUR, -18, CURRENT_TIMESTAMP)),
-- 帖子4（回锅肉）3 条
(11, 2, 4, NULL, 3, '灯盏窝卷得漂亮！这盘下三碗饭没问题', NULL, 11, 1, DATEADD(HOUR, -22, CURRENT_TIMESTAMP)),
(12, 2, 4, NULL, 7, '蒜苗分两次下是关键，看来是老手了', NULL, 5, 1, DATEADD(HOUR, -20, CURRENT_TIMESTAMP)),
(13, 2, 4, NULL, 6, '减脂明天再说哈哈哈哈，今天先干饭', NULL, 9, 1, DATEADD(HOUR, -18, CURRENT_TIMESTAMP)),
-- 帖子5（可乐鸡翅）3 条
(14, 2, 5, NULL, 2, '小姑娘手艺不错，划刀腌制确实入味', NULL, 7, 1, DATEADD(HOUR, -20, CURRENT_TIMESTAMP)),
(15, 2, 5, NULL, 4, '收藏了，做给弟弟吃，他就爱这口甜的', NULL, 4, 1, DATEADD(HOUR, -16, CURRENT_TIMESTAMP)),
(16, 2, 5, NULL, 5, '提醒一句收汁要勤翻面，我上次糊底了哈哈', NULL, 6, 1, DATEADD(HOUR, -12, CURRENT_TIMESTAMP)),
-- 帖子6（清蒸鲈鱼）2 条
(17, 2, 6, NULL, 2, '蒸鱼时间就是一切，王叔说到点子上了', NULL, 8, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(18, 2, 6, NULL, 6, '筷子架空鱼身这个细节学到了！', NULL, 5, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
-- 帖子7（番茄炒蛋日记）2 条
(19, 2, 7, NULL, 6, '简简单单才是家的味道，婆婆的日记好治愈', NULL, 10, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(20, 2, 7, NULL, 5, '番茄去皮派 +1，口感真的细腻很多', NULL, 4, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
-- 帖子8（扬州炒饭日记）2 条
(21, 2, 8, NULL, 3, '一个人也要好好吃饭，这句先干为敬', NULL, 12, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(22, 2, 8, NULL, 4, '米粒会跳是真的，大火快翻就是炒饭的灵魂', NULL, 6, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
-- 帖子9（剁椒鱼头）3 条
(23, 2, 9, NULL, 5, '最后下面条这个收尾方式，懂行！', NULL, 13, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(24, 2, 9, NULL, 7, '虚蒸三分钟鱼脑才嫩，这个细节满分', NULL, 7, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(25, 2, 9, NULL, 2, '看着就辣得过瘾，可惜我家老头子吃不了辣', NULL, 3, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
-- 帖子10（健身餐日记）2 条
(26, 2, 10, NULL, 4, '同减脂人，互相监督！你的摆盘比我精致', NULL, 5, 1, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(27, 2, 10, NULL, 3, '练完20分钟出餐，效率太高了', NULL, 4, 1, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
-- 帖子11（切配经验）3 条
(28, 2, 11, NULL, 2, '猫爪手势讲得对，我教孙女也是这么教的', NULL, 14, 1, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(29, 2, 11, NULL, 6, '盐搓手去味亲测有效，再也不怕切完蒜手臭了', NULL, 8, 1, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(30, 2, 11, NULL, 5, '建议加一条：刀钝了比刀快更容易切到手', NULL, 11, 1, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
-- 帖子12（麻婆豆腐）2 条
(31, 2, 12, NULL, 3, '八字真言全占，王叔这碗我打满分', NULL, 9, 1, DATEADD(DAY, -5, CURRENT_TIMESTAMP)),
(32, 2, 12, NULL, 4, '花椒粉起锅再撒，记下了！', NULL, 5, 1, DATEADD(DAY, -5, CURRENT_TIMESTAMP)),
-- 帖子13（炒糖色经验）4 条
(33, 2, 13, NULL, 2, '失败三次换来的经验最值钱，我当年熬糊了一整锅', NULL, 16, 1, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(34, 2, 13, NULL, 6, '鱼眼泡这个形容太形象了，新手秒懂', NULL, 9, 1, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(35, 2, 13, NULL, 7, '离火再下肉确实稳，怕溅油的都试试', NULL, 7, 1, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(36, 2, 13, NULL, 3, '收藏了，这周做红烧肉就靠它了', NULL, 5, 1, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
-- 帖子14（宫保鸡丁）2 条
(37, 2, 14, NULL, 5, '荔枝口形容得真准，酸甜里那点糊辣香最迷人', NULL, 8, 1, DATEADD(DAY, -7, CURRENT_TIMESTAMP)),
(38, 2, 14, NULL, 7, '碗汁党永远的神，手忙脚乱救星', NULL, 6, 1, DATEADD(DAY, -7, CURRENT_TIMESTAMP)),
-- 菜谱评论（target_type=1，菜谱详情页展示）10 条
(39, 1, 11, NULL, 2, '炖足40分钟真的入口即化，家里老人牙口不好也吃得动', NULL, 21, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(40, 1, 11, NULL, 5, '按这个比例做了两次，咸甜刚好，不用再调整', NULL, 13, 1, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(41, 1, 12, NULL, 6, '人生第一道菜打卡成功，七成熟盛蛋这步太重要了', NULL, 18, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(42, 1, 15, NULL, 3, '鱼片腌足10分钟真的嫩，加蛋清是灵魂', NULL, 15, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(43, 1, 15, NULL, 7, '酸菜冲洗两遍这步别省，不然咸得发苦', NULL, 9, 1, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(44, 1, 18, NULL, 6, '新手第一次做就成功，男朋友说像饭店买的', NULL, 17, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(45, 1, 17, NULL, 4, '低卡高蛋白，减脂期一周吃三次都不腻', NULL, 11, 1, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(46, 1, 1, NULL, 5, '90秒收汁计时太贴心，跟着做鸡丁一点不柴', NULL, 14, 1, DATEADD(DAY, -5, CURRENT_TIMESTAMP)),
(47, 1, 16, NULL, 2, '隔夜饭打散这个提示好，现煮的饭炒出来就是坨', NULL, 10, 1, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(48, 1, 2, NULL, 4, '豆腐盐水焯过果然不碎了，勾两次芡也学到了', NULL, 12, 1, DATEADD(DAY, -6, CURRENT_TIMESTAMP));

-- ---------------------------------------------------------------
-- 4. 菜谱评分 social_rating（18 条；user_id+recipe_id 唯一）
-- ---------------------------------------------------------------
INSERT INTO social_rating (id, user_id, recipe_id, score, created_at) VALUES
(1,  2, 11, 5, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(2,  5, 11, 5, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(3,  3, 15, 5, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(4,  7, 15, 4, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(5,  6, 18, 5, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(6,  4, 18, 4, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(7,  6, 12, 5, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(8,  2, 12, 5, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(9,  4, 17, 5, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(10, 7, 17, 5, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(11, 5, 16, 5, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(12, 2, 16, 4, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(13, 5, 1,  5, DATEADD(DAY, -5, CURRENT_TIMESTAMP)),
(14, 2, 1,  4, DATEADD(DAY, -8, CURRENT_TIMESTAMP)),
(15, 4, 2,  5, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(16, 7, 2,  5, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(17, 3, 8,  5, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(18, 4, 19, 4, DATEADD(DAY, -1, CURRENT_TIMESTAMP));

-- ---------------------------------------------------------------
-- 5. 烹饪打卡 social_checkin（user_id+checkin_date 唯一；continuous_days 与日期连续性一致）
-- u4 青菜不青连续 5 天；u5 翻锅飞侠连续 7 天（已领七日烟火徽章）；其余零散
-- ---------------------------------------------------------------
INSERT INTO social_checkin (id, user_id, checkin_date, recipe_id, note, continuous_days, created_at) VALUES
-- 用户2：连续 2 天
(1,  2, DATEADD(DAY, -1, CURRENT_DATE), 12, '番茄炒蛋，老两口的晚饭', 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(2,  2, CURRENT_DATE,                   11, '红烧肉一次成功，开心', 2, CURRENT_TIMESTAMP),
-- 用户3：昨天 1 天
(3,  3, DATEADD(DAY, -1, CURRENT_DATE), 15, '深夜酸菜鱼，加班后的犒劳', 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
-- 用户4：连续 5 天（减脂餐）
(4,  4, DATEADD(DAY, -4, CURRENT_DATE), 10, '鸡胸沙拉日常', 1, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(5,  4, DATEADD(DAY, -3, CURRENT_DATE), 19, '西兰花换换口味', 2, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(6,  4, DATEADD(DAY, -2, CURRENT_DATE), 17, '清蒸鲈鱼补蛋白', 3, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(7,  4, DATEADD(DAY, -1, CURRENT_DATE), 10, '练腿日多吃一份鸡胸', 4, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(8,  4, CURRENT_DATE,                   19, '减脂餐第43天', 5, CURRENT_TIMESTAMP),
-- 用户5：连续 7 天（解锁七日烟火）
(9,  5, DATEADD(DAY, -6, CURRENT_DATE), 13, '鱼香肉丝练手', 1, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(10, 5, DATEADD(DAY, -5, CURRENT_DATE), 11, '炒糖色第四次，稳了', 2, DATEADD(DAY, -5, CURRENT_TIMESTAMP)),
(11, 5, DATEADD(DAY, -4, CURRENT_DATE), 16, '扬州炒饭一人食', 3, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(12, 5, DATEADD(DAY, -3, CURRENT_DATE), 2,  '麻婆豆腐下饭神器', 4, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(13, 5, DATEADD(DAY, -2, CURRENT_DATE), 14, '回锅肉灯盏窝成功', 5, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(14, 5, DATEADD(DAY, -1, CURRENT_DATE), 1,  '宫保鸡丁复刻', 6, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(15, 5, CURRENT_DATE,                   18, '可乐鸡翅，今天偷个懒', 7, CURRENT_TIMESTAMP),
-- 用户6：今天 1 天
(16, 6, CURRENT_DATE,                   18, '第一次做鸡翅就被夸', 1, CURRENT_TIMESTAMP),
-- 用户7：连续 3 天
(17, 7, DATEADD(DAY, -2, CURRENT_DATE), 17, '周末蒸鱼', 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(18, 7, DATEADD(DAY, -1, CURRENT_DATE), 2,  '麻婆豆腐配米饭', 2, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(19, 7, CURRENT_DATE,                   4,  '白切鸡，给孙子补补', 3, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 6. 用户已获徽章 social_user_badge（user_id+badge_id 唯一）
-- ---------------------------------------------------------------
INSERT INTO social_user_badge (id, user_id, badge_id, obtained_at) VALUES
(1,  2, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(2,  2, 4, DATEADD(DAY, -8, CURRENT_TIMESTAMP)),
(3,  3, 1, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(4,  3, 4, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(5,  4, 1, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(6,  4, 4, DATEADD(DAY, -5, CURRENT_TIMESTAMP)),
(7,  5, 1, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(8,  5, 2, CURRENT_TIMESTAMP),
(9,  5, 4, DATEADD(DAY, -7, CURRENT_TIMESTAMP)),
(10, 6, 1, CURRENT_TIMESTAMP),
(11, 6, 4, DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(12, 7, 1, DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(13, 7, 4, DATEADD(DAY, -2, CURRENT_TIMESTAMP));

-- ---------------------------------------------------------------
-- 7. 点赞记录 social_like_record（user_id+target_type+target_id 唯一）
-- target_type：1帖子 2评论 3菜谱；demo 用户(1)不预置，便于演示点赞交互
-- ---------------------------------------------------------------
INSERT INTO social_like_record (id, user_id, target_type, target_id, created_at) VALUES
(1,  3, 1, 1,  DATEADD(HOUR, -4, CURRENT_TIMESTAMP)),
(2,  5, 1, 1,  DATEADD(HOUR, -4, CURRENT_TIMESTAMP)),
(3,  6, 1, 1,  DATEADD(HOUR, -3, CURRENT_TIMESTAMP)),
(4,  4, 1, 2,  DATEADD(HOUR, -8, CURRENT_TIMESTAMP)),
(5,  7, 1, 2,  DATEADD(HOUR, -7, CURRENT_TIMESTAMP)),
(6,  6, 1, 3,  DATEADD(HOUR, -20, CURRENT_TIMESTAMP)),
(7,  3, 1, 4,  DATEADD(HOUR, -22, CURRENT_TIMESTAMP)),
(8,  2, 1, 5,  DATEADD(HOUR, -20, CURRENT_TIMESTAMP)),
(9,  4, 1, 5,  DATEADD(HOUR, -16, CURRENT_TIMESTAMP)),
(10, 2, 1, 6,  DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(11, 6, 1, 7,  DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
(12, 3, 1, 8,  DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(13, 5, 1, 9,  DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(14, 7, 1, 9,  DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
(15, 4, 1, 10, DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
(16, 2, 1, 11, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(17, 6, 1, 11, DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
(18, 3, 1, 12, DATEADD(DAY, -5, CURRENT_TIMESTAMP)),
(19, 2, 1, 13, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(20, 6, 1, 13, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(21, 7, 1, 13, DATEADD(DAY, -6, CURRENT_TIMESTAMP)),
(22, 5, 1, 14, DATEADD(DAY, -7, CURRENT_TIMESTAMP));

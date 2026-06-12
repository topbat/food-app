-- =====================================================================
-- 食研社 recipe-service dev 种子数据（H2 MySQL 模式，启动自动执行）
-- 说明：纯 INSERT；显式指定主键 id；日期列统一 CURRENT_TIMESTAMP
-- 表顺序：食材库 -> 标签 -> 菜谱 -> 步骤 -> 菜谱食材 -> 标签关联 -> 替换规则
-- 图片素材：统一使用前端静态资源 /images/**（与菜品一一对应的实拍图）
--   封面 /images/recipes/<菜名拼音>.jpg；步骤图按阶段使用 /images/steps/phase-*.jpg；
--   装盘（PLATE）步骤直接复用该菜成品图。
-- =====================================================================

-- ---------------------------------------------------------------
-- 1. 基础食材库 recipe_ingredient_lib（48 条）
-- 分类：肉类/水产/蛋类/蔬菜/豆制品/坚果/调料/油脂/主食
-- ---------------------------------------------------------------
INSERT INTO recipe_ingredient_lib (id, name, category, calories_per_100g, carbs_per_100g, protein_per_100g, fat_per_100g, taboo_note, created_at) VALUES
(1,  '鸡胸肉', '肉类', 133.00, 2.50,  24.00, 5.00,  NULL, CURRENT_TIMESTAMP),
(2,  '五花肉', '肉类', 568.00, 0.00,  13.00, 53.00, NULL, CURRENT_TIMESTAMP),
(3,  '猪里脊', '肉类', 155.00, 0.00,  20.20, 7.90,  NULL, CURRENT_TIMESTAMP),
(4,  '猪肉末', '肉类', 395.00, 0.00,  14.00, 37.00, NULL, CURRENT_TIMESTAMP),
(5,  '三黄鸡', '肉类', 167.00, 0.00,  20.00, 9.50,  NULL, CURRENT_TIMESTAMP),
(6,  '豆腐',   '豆制品', 81.00, 4.20,  8.10,  3.70,  NULL, CURRENT_TIMESTAMP),
(7,  '鲈鱼',   '水产', 105.00, 0.00,  18.60, 3.40,  '嘌呤较高，痛风人群慎食', CURRENT_TIMESTAMP),
(8,  '鳜鱼',   '水产', 117.00, 0.00,  19.90, 4.20,  '嘌呤较高，痛风人群慎食', CURRENT_TIMESTAMP),
(9,  '草鱼',   '水产', 113.00, 0.00,  16.60, 5.20,  '嘌呤较高，痛风人群慎食', CURRENT_TIMESTAMP),
(10, '胖头鱼', '水产', 100.00, 0.00,  15.30, 2.20,  '嘌呤较高，痛风人群慎食', CURRENT_TIMESTAMP),
(11, '虾仁',   '水产', 48.00,  0.00,  10.40, 0.70,  '嘌呤较高，痛风人群慎食；海鲜过敏者忌食', CURRENT_TIMESTAMP),
(12, '鸡蛋',   '蛋类', 144.00, 2.80,  13.30, 8.80,  NULL, CURRENT_TIMESTAMP),
(13, '西红柿', '蔬菜', 20.00,  4.00,  0.90,  0.20,  NULL, CURRENT_TIMESTAMP),
(14, '黄瓜',   '蔬菜', 16.00,  2.90,  0.80,  0.20,  NULL, CURRENT_TIMESTAMP),
(15, '胡萝卜', '蔬菜', 39.00,  8.80,  1.00,  0.20,  NULL, CURRENT_TIMESTAMP),
(16, '土豆',   '蔬菜', 77.00,  17.20, 2.00,  0.20,  NULL, CURRENT_TIMESTAMP),
(17, '青椒',   '蔬菜', 22.00,  5.40,  1.00,  0.20,  NULL, CURRENT_TIMESTAMP),
(18, '西兰花', '蔬菜', 36.00,  4.30,  4.10,  0.60,  NULL, CURRENT_TIMESTAMP),
(19, '生菜',   '蔬菜', 15.00,  2.00,  1.30,  0.30,  NULL, CURRENT_TIMESTAMP),
(20, '马蹄',   '蔬菜', 61.00,  14.20, 1.20,  0.10,  NULL, CURRENT_TIMESTAMP),
(21, '干辣椒', '调料', 298.00, 57.00, 15.00, 12.00, NULL, CURRENT_TIMESTAMP),
(22, '剁椒',   '调料', 31.00,  6.00,  1.60,  0.60,  NULL, CURRENT_TIMESTAMP),
(23, '豆瓣酱', '调料', 178.00, 17.10, 6.90,  9.80,  '钠含量高，高血压人群慎食', CURRENT_TIMESTAMP),
(24, '花椒',   '调料', 258.00, 66.50, 6.70,  8.90,  NULL, CURRENT_TIMESTAMP),
(25, '花生米', '坚果', 567.00, 16.20, 24.80, 44.30, NULL, CURRENT_TIMESTAMP),
(26, '大葱',   '蔬菜', 33.00,  6.50,  1.70,  0.30,  NULL, CURRENT_TIMESTAMP),
(27, '姜',     '调料', 46.00,  10.30, 1.30,  0.60,  NULL, CURRENT_TIMESTAMP),
(28, '蒜',     '调料', 128.00, 27.60, 4.50,  0.20,  NULL, CURRENT_TIMESTAMP),
(29, '生抽',   '调料', 63.00,  10.10, 5.60,  0.10,  NULL, CURRENT_TIMESTAMP),
(30, '醋',     '调料', 31.00,  4.90,  2.10,  0.30,  NULL, CURRENT_TIMESTAMP),
(31, '白糖',   '调料', 400.00, 99.90, 0.00,  0.00,  '控糖人群慎食', CURRENT_TIMESTAMP),
(32, '木糖醇', '调料', 240.00, 99.50, 0.00,  0.00,  NULL, CURRENT_TIMESTAMP),
(33, '淀粉',   '调料', 346.00, 85.00, 0.50,  0.10,  NULL, CURRENT_TIMESTAMP),
(34, '食用油', '油脂', 899.00, 0.00,  0.00,  99.90, NULL, CURRENT_TIMESTAMP),
(35, '料酒',   '调料', 66.00,  4.70,  0.70,  0.00,  NULL, CURRENT_TIMESTAMP),
(36, '盐',     '调料', 0.00,   0.00,  0.00,  0.00,  NULL, CURRENT_TIMESTAMP),
(37, '木耳',   '蔬菜', 27.00,  6.00,  1.50,  0.20,  NULL, CURRENT_TIMESTAMP),
(38, '竹笋',   '蔬菜', 23.00,  3.60,  2.60,  0.20,  NULL, CURRENT_TIMESTAMP),
(39, '蒜苗',   '蔬菜', 37.00,  8.00,  2.10,  0.40,  NULL, CURRENT_TIMESTAMP),
(40, '鸡翅',   '肉类', 194.00, 4.60,  17.40, 11.80, NULL, CURRENT_TIMESTAMP),
(41, '可乐',   '调料', 43.00,  10.60, 0.00,  0.00,  '含糖饮料，控糖人群慎食', CURRENT_TIMESTAMP),
(42, '米饭',   '主食', 116.00, 25.90, 2.60,  0.30,  NULL, CURRENT_TIMESTAMP),
(43, '火腿',   '肉类', 330.00, 4.90,  16.00, 27.40, '钠含量高，高血压人群慎食', CURRENT_TIMESTAMP),
(44, '豌豆',   '蔬菜', 111.00, 21.20, 7.40,  0.30,  NULL, CURRENT_TIMESTAMP),
(45, '酸菜',   '蔬菜', 14.00,  2.20,  1.10,  0.20,  '腌制食品钠含量高，高血压人群慎食', CURRENT_TIMESTAMP),
(46, '老抽',   '调料', 129.00, 29.00, 5.60,  0.10,  NULL, CURRENT_TIMESTAMP),
(47, '八角',   '调料', 281.00, 75.40, 3.80,  5.60,  NULL, CURRENT_TIMESTAMP),
(48, '冰糖',   '调料', 397.00, 99.30, 0.00,  0.00,  '控糖人群慎食', CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 2. 标签字典 recipe_tag（18 条；tag_type：1人群 2功效 3场景）
-- ---------------------------------------------------------------
INSERT INTO recipe_tag (id, tag_name, tag_type, created_at) VALUES
(1,  '减脂期',     1, CURRENT_TIMESTAMP),
(2,  '增肌期',     1, CURRENT_TIMESTAMP),
(3,  '控糖人群',   1, CURRENT_TIMESTAMP),
(4,  '素食者',     1, CURRENT_TIMESTAMP),
(5,  '儿童适宜',   1, CURRENT_TIMESTAMP),
(6,  '老人适宜',   1, CURRENT_TIMESTAMP),
(7,  '孕妇慎用',   1, CURRENT_TIMESTAMP),
(8,  '痛风慎用',   1, CURRENT_TIMESTAMP),
(9,  '高蛋白',     2, CURRENT_TIMESTAMP),
(10, '低卡',       2, CURRENT_TIMESTAMP),
(11, '补气血',     2, CURRENT_TIMESTAMP),
(12, '快手菜',     3, CURRENT_TIMESTAMP),
(13, '下饭菜',     3, CURRENT_TIMESTAMP),
(14, '熬夜党',     3, CURRENT_TIMESTAMP),
(15, '健身后',     3, CURRENT_TIMESTAMP),
(16, '生理期友好', 3, CURRENT_TIMESTAMP),
(17, '一人食',     3, CURRENT_TIMESTAMP),
(18, '宴客菜',     3, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 3. 菜谱主表 recipe_info（19 道；status=1 上架，source_type=1 官方）
-- 菜系：1川 2鲁 3粤 4苏 5闽 6浙 7湘 8徽 9家常
-- ---------------------------------------------------------------
INSERT INTO recipe_info (id, title, cover_url, cuisine_type, difficulty, total_time_min, servings, calories_kcal, carbs_g, protein_g, fat_g, description, tips, status, view_count, like_count, author_id, source_type, created_at, updated_at) VALUES
(1, '宫保鸡丁', '/images/recipes/gongbao-jiding.jpg', 1, 2, 25, 2, 350.00, 15.00, 30.00, 18.00,
 '川菜宫保系的当家花旦，相传得名于清末四川总督丁宝桢的官衔太子少保。鸡丁滑嫩、花生酥香，荔枝口的小酸甜里藏着糊辣香。',
 '干辣椒去籽可防辣手呛喉；碗汁提前调好，下锅后大火快炒不超过90秒，鸡丁才嫩。', 1, 1280, 326, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '麻婆豆腐', '/images/recipes/mapo-doufu.jpg', 1, 1, 20, 2, 280.00, 12.00, 16.00, 19.00,
 '同治年间成都万福桥边陈麻婆的看家菜，麻、辣、烫、香、酥、嫩、鲜、活八字真言，一勺浇在米饭上便是人间至味。',
 '豆腐先用淡盐水焯1分钟可去豆腥且不易碎；勾芡分两次进行，汁更亮更裹味。', 1, 1500, 412, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '糖醋里脊', '/images/recipes/tangcu-liji.jpg', 2, 2, 30, 2, 420.00, 38.00, 22.00, 20.00,
 '鲁菜糖醋技法的入门名作，金黄酥壳裹着嫩肉，糖醋汁亮如琥珀，是无数人童年记忆里的第一道硬菜。',
 '复炸是酥脆的关键：第一遍定型，第二遍升高油温逼出余油；糖醋汁熬至冒大泡再下肉。', 1, 950, 268, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '白切鸡', '/images/recipes/baiqie-ji.jpg', 3, 2, 45, 4, 310.00, 2.00, 28.00, 21.00,
 '粤菜以鸡为尊，无鸡不成宴。白切鸡讲究皮爽肉滑、骨髓带红，一碟姜葱蓉是它最忠实的伴侣。',
 '浸煮全程保持虾眼水（约90度微沸）不翻滚；煮好立刻入冰水，皮才会爽脆收紧。', 1, 860, 240, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '松鼠桂鱼', '/images/recipes/songshu-guiyu.jpg', 4, 3, 50, 3, 380.00, 30.00, 26.00, 18.00,
 '苏帮菜的状元郎，乾隆下江南的传说为它添了三分贵气。改刀成菊、入油成松鼠，浇汁时滋啦一声恰似松鼠鸣叫。',
 '剞花刀深至鱼皮但不能切破；拍粉后抖去余粉，炸出的花刀才根根分明。', 1, 530, 188, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '荔枝肉', '/images/recipes/lizhi-rou.jpg', 5, 2, 35, 2, 390.00, 35.00, 20.00, 19.00,
 '闽菜里有名的有荔枝之形而无荔枝之实的巧菜，十字花刀的里脊炸后卷曲如荔枝，配马蹄同烧，酸甜里带着脆爽。',
 '花刀切得越细密，炸后卷曲越像荔枝；马蹄最后下锅，保持脆感。', 1, 410, 132, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '西湖醋鱼', '/images/recipes/xihu-cuyu.jpg', 6, 2, 30, 2, 230.00, 12.00, 24.00, 9.00,
 '杭州楼外楼的镇店之宝，宋嫂传艺的典故流传八百年。不用一滴油的氽煮技法，糖醋汁里要吃出蟹味，才算正宗。',
 '草鱼氽煮以筷子能轻松插入鱼背为度，多一分则老；糖醋汁中姜末不可省，去腥提鲜全靠它。', 1, 640, 175, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, '剁椒鱼头', '/images/recipes/duojiao-yutou.jpg', 7, 2, 40, 3, 260.00, 6.00, 25.00, 15.00,
 '湘菜的鸿运当头，红艳艳的剁椒铺满胖头鱼头，蒸汽裹着发酵辣香钻进鱼肉的每一丝纹理，配碗面条收尾才算圆满。',
 '鱼头开边后脊骨处划一刀更易蒸透；出锅后淋热油激香是点睛之笔，油温要冒烟。', 1, 880, 295, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, '黄山臭鳜鱼', '/images/recipes/chou-guiyu.jpg', 8, 3, 60, 3, 290.00, 5.00, 27.00, 17.00,
 '徽商沿新安江贩鱼，木桶淡盐水腌出的似臭非臭成就了徽菜头牌。闻着微臭、吃着透鲜，鱼肉呈蒜瓣状才是上品。',
 '煎鱼前用厨房纸吸干表面水分防溅油；烧制时加少许五花肉丁，鱼肉更润更香。', 1, 360, 120, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, '低卡鸡胸时蔬沙拉', '/images/recipes/jixiong-shala.jpg', 9, 1, 15, 1, 210.00, 10.00, 30.00, 6.00,
 '健身人的快手家常担当，水煮鸡胸撕成细丝拌入五彩时蔬，高蛋白低脂肪，一碗吃出轻盈感。',
 '鸡胸煮好后在汤中浸5分钟再捞出，肉质不柴；油醋汁现拌现吃，蔬菜不出水。', 1, 1100, 358, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, '红烧肉', '/images/recipes/hongshao-rou.jpg', 9, 2, 90, 3, 580.00, 12.00, 14.00, 50.00,
 '中国人餐桌上最有分量的一碗肉。冰糖炒出的琥珀糖色裹着颤巍巍的五花肉，肥而不腻、瘦而不柴，再焖几枚卤蛋吸饱汤汁，一锅端上桌就是过年的气氛。',
 '炒糖色到琥珀色冒小泡立刻下肉，多三秒就发苦；中途加水务必加热水，肉遇冷水会发紧变柴。', 1, 1620, 458, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, '番茄炒蛋', '/images/recipes/fanqie-chaodan.jpg', 9, 1, 10, 2, 180.00, 8.00, 10.00, 12.00,
 '国民下饭菜的无冕之王，也是无数人学会的第一道菜。蛋要嫩、茄要沙，红黄交融的浓汁往米饭上一浇，简单到极致也好吃到极致。',
 '鸡蛋七成熟就盛出，最后回锅才嫩；番茄去皮口感更细腻，加一小勺糖能吊出果香。', 1, 2100, 566, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, '鱼香肉丝', '/images/recipes/yuxiang-rousi.jpg', 1, 2, 25, 2, 320.00, 18.00, 22.00, 17.00,
 '无鱼却有鱼香，是川菜复合调味的巅峰之作。咸甜酸辣兼备，姜葱蒜香突出，肉丝滑嫩、木耳脆爽，一勺汁能扒拉下两碗米饭。',
 '鱼香汁的黄金比例约为糖醋各一、生抽减半；肉丝顺纹切、逆纹炒，上浆后过油更滑嫩。', 1, 1380, 392, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, '回锅肉', '/images/recipes/huiguo-rou.jpg', 1, 2, 40, 2, 450.00, 8.00, 16.00, 40.00,
 '川菜之首不是虚名。先煮后炒的两道工序让五花肉卷成灯盏窝，豆瓣酱的红亮裹着蒜苗的辛香，是川人刻在骨子里的家乡味。',
 '肉片煸出灯盏窝状再下豆瓣酱；蒜苗秆先下、叶后下，断生即起锅，香气最足。', 1, 1150, 334, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, '酸菜鱼', '/images/recipes/suancai-yu.jpg', 1, 2, 45, 3, 230.00, 5.00, 26.00, 11.00,
 '重庆江湖菜的代表作，老坛酸菜的乳酸香衬着雪白鱼片的嫩滑。先喝一口酸辣开胃的汤，再涮一筷子入口即化的鱼片，从胃暖到心。',
 '鱼片加蛋清和淀粉抓匀腌10分钟是嫩滑的关键；鱼片下锅后不要翻动，晃锅让它自然定型。', 1, 1480, 415, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, '扬州炒饭', '/images/recipes/yangzhou-chaofan.jpg', 4, 1, 20, 1, 410.00, 58.00, 15.00, 12.00,
 '一碗有据可查的非遗炒饭。金裹银的蛋香打底，虾仁、火腿、豌豆点缀其间，粒粒分明又粒粒入味，是一个人也要好好吃饭的最佳证明。',
 '隔夜饭提前打散回温；先炒蛋后下饭，全程大火快翻，米粒在锅里跳起来才算合格。', 1, 1750, 478, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, '清蒸鲈鱼', '/images/recipes/qingzheng-luyu.jpg', 3, 1, 25, 2, 190.00, 3.00, 25.00, 8.00,
 '粤菜蒸鱼的标准答案：八分钟大火足汽，鱼肉刚离骨、筷子一拨就开。姜葱丝铺面，热油一激、豉油一淋，鲜字就这样写在盘子里。',
 '蒸鱼前用筷子把鱼身架空，受热更均匀；蒸鱼豉油沿盘边淋入，不要直接浇在鱼肉上。', 1, 1290, 365, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, '可乐鸡翅', '/images/recipes/kele-jichi.jpg', 9, 1, 30, 2, 320.00, 18.00, 22.00, 16.00,
 '厨房新手的第一道成名作。可乐的焦糖甜替代炒糖色，鸡翅焖得油亮酱红、轻轻一咬就脱骨，是大人小孩都抢着吃的快乐硬菜。',
 '鸡翅两面划刀更入味；收汁阶段勤翻面防糊底，汤汁能挂勺背即可关火。', 1, 1850, 502, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, '蒜蓉西兰花', '/images/recipes/suanrong-xilanhua.jpg', 3, 1, 12, 2, 95.00, 8.00, 5.00, 5.00,
 '减脂餐里最不像减脂餐的一道绿。焯水锁住翠绿与脆嫩，蒜末爆香后大火快炒三十秒，清爽里带着锅气，刷脂期也吃得有滋有味。',
 '焯水时加几滴油和少许盐，西兰花更翠绿；蒜末分两次下，一半爆香一半起锅前撒，蒜香更立体。', 1, 980, 276, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 4. 菜谱步骤 recipe_step（136 条；五步法 PREPARE/WASH/CUT/COOK/PLATE）
-- 步骤图按阶段取材：备料/清洗/切配用通用实拍图，烹煮轮换三张灶上图，装盘用成品图
-- ---------------------------------------------------------------
-- 菜谱1：宫保鸡丁（8 步，参考产品需求文档五步法示例）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(1, 1, 'PREPARE', 1, '食材称重与腌制', '鸡胸肉150克切1.5厘米见方小丁，加生抽10毫升、料酒5毫升、淀粉3克抓匀，腌制15分钟入味。', '/images/steps/phase-prepare.jpg', 900, NULL, CURRENT_TIMESTAMP),
(2, 1, 'PREPARE', 2, '调配碗汁', '取小碗，依次加入醋20毫升、白糖10克、生抽10毫升、淀粉5克、清水30毫升，搅拌至白糖完全融化备用。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(3, 1, 'WASH', 1, '清洗配菜', '黄瓜80克、胡萝卜50克流水洗净后去皮；大葱剥去外层老皮冲洗干净；花生米拣去坏粒。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(4, 1, 'CUT', 1, '改刀切配', '黄瓜、胡萝卜切1厘米见方小丁；大葱取葱白切2厘米葱节；干辣椒8克剪成段并去籽防辣手。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(5, 1, 'COOK', 1, '滑炒鸡丁', '热锅倒食用油20毫升，五成油温（木筷插入冒小泡）下腌好的鸡丁滑散，炒约2分钟至表面变白盛出。', '/images/steps/phase-cook-1.jpg', 120, '中火', CURRENT_TIMESTAMP),
(6, 1, 'COOK', 2, '爆香翻炒', '利用锅中余油爆香干辣椒段与花椒3克，闻到糊辣香后下葱节、胡萝卜丁翻炒，再倒回鸡丁与黄瓜丁炒匀，约1分钟。', '/images/steps/phase-cook-2.jpg', 60, '大火', CURRENT_TIMESTAMP),
(7, 1, 'COOK', 3, '倒汁收汁', '将碗汁搅匀后沿锅边淋入，大火快速翻炒约90秒，至汤汁浓稠发亮、均匀挂在食材表面。', '/images/steps/phase-cook-3.jpg', 90, '大火', CURRENT_TIMESTAMP),
(8, 1, 'PLATE', 1, '摆盘出餐', '关火后拌入花生米30克（保持酥脆），盛入深盘中心堆成小山状，趁热上桌。', '/images/recipes/gongbao-jiding.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱2：麻婆豆腐（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(9,  2, 'PREPARE', 1, '称重备料', '嫩豆腐400克、猪肉末80克、豆瓣酱20克、花椒5克分别备好；淀粉8克加清水30毫升调成水淀粉。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(10, 2, 'WASH', 1, '清洗辅料', '大葱、蒜流水冲洗干净；豆腐整块用清水轻轻冲淋一遍沥干。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(11, 2, 'CUT', 1, '切块切末', '豆腐切2厘米见方小块；大葱20克切葱花；蒜10克切末；豆瓣酱在砧板上剁细更易出红油。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(12, 2, 'COOK', 1, '豆腐焯水', '锅中烧水加盐2克，水开后下豆腐块焯1分钟去豆腥并定型，捞出沥水。', '/images/steps/phase-cook-1.jpg', 60, '中火', CURRENT_TIMESTAMP),
(13, 2, 'COOK', 2, '炒酥肉末', '锅中倒食用油20毫升，下猪肉末炒散至微微发酥，加入豆瓣酱与蒜末，中火炒出红油约90秒。', '/images/steps/phase-cook-2.jpg', 90, '中火', CURRENT_TIMESTAMP),
(14, 2, 'COOK', 3, '烧制勾芡', '加清水200毫升烧开，轻轻推入豆腐块小火烧3分钟，分两次淋入水淀粉勾芡至汤汁浓亮。', '/images/steps/phase-cook-3.jpg', 180, '小火', CURRENT_TIMESTAMP),
(15, 2, 'PLATE', 1, '撒料装盘', '盛入深口碗中，趁热撒上现磨花椒粉与葱花，麻香随热气升腾即可上桌。', '/images/recipes/mapo-doufu.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱3：糖醋里脊（8 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(16, 3, 'PREPARE', 1, '腌制里脊', '猪里脊250克备好，加料酒10毫升、盐1克抓匀腌制10分钟去腥入底味。', '/images/steps/phase-prepare.jpg', 600, NULL, CURRENT_TIMESTAMP),
(17, 3, 'PREPARE', 2, '调糖醋汁与面糊', '糖醋汁：白糖30克、醋25毫升、生抽10毫升、淀粉5克、清水40毫升调匀。面糊：淀粉35克加鸡蛋1个搅成酸奶状稠糊。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(18, 3, 'WASH', 1, '清洗沥干', '里脊冲洗后用厨房纸彻底吸干表面水分，挂糊才牢固、炸时不溅油。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(19, 3, 'CUT', 1, '切条挂糊', '里脊顺纹切成1.5厘米粗、6厘米长的条，逐条裹满面糊。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(20, 3, 'COOK', 1, '初炸定型', '食用油烧至六成热（约180度），逐条下入里脊条炸2分钟至浅黄定型，捞出沥油。', '/images/steps/phase-cook-1.jpg', 120, '中火', CURRENT_TIMESTAMP),
(21, 3, 'COOK', 2, '复炸酥脆', '油温升至八成热（油面轻微冒烟），倒回里脊条复炸约60秒至金黄酥脆，迅速捞出。', '/images/steps/phase-cook-2.jpg', 60, '大火', CURRENT_TIMESTAMP),
(22, 3, 'COOK', 3, '熬汁裹匀', '锅留底油，倒入糖醋汁中火熬至冒大泡，下炸好的里脊快速颠锅翻匀，约90秒让每条都裹上亮汁。', '/images/steps/phase-cook-3.jpg', 90, '中火', CURRENT_TIMESTAMP),
(23, 3, 'PLATE', 1, '装盘点缀', '趁汁未凝快速装盘，码放整齐，可点缀几片黄瓜解腻。', '/images/recipes/tangcu-liji.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱4：白切鸡（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(24, 4, 'PREPARE', 1, '备鸡回温', '三黄鸡1只约1000克提前30分钟从冰箱取出回至室温，避免冷鸡下锅导致受热不均；同时备姜30克、大葱30克。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(25, 4, 'WASH', 1, '清洗整鸡', '整鸡里外冲洗干净，重点冲净腹腔血水与残留内脏膜，沥干水分。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(26, 4, 'CUT', 1, '切姜葱', '姜一半切厚片入锅用，一半切细蓉做蘸料；大葱葱段入锅，葱白部分切细蓉与姜蓉混合，加盐2克。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(27, 4, 'COOK', 1, '虾眼水浸煮', '大锅水烧开后放姜片葱段，提鸡头三起三落后整鸡入锅，转小火保持约90度微沸状态浸煮15分钟。', '/images/steps/phase-cook-1.jpg', 900, '小火', CURRENT_TIMESTAMP),
(28, 4, 'COOK', 2, '冰水过凉', '关火后将鸡迅速捞入冰水中浸10分钟，热胀冷缩让鸡皮爽脆、肉质收紧。', '/images/steps/phase-cook-2.jpg', 600, NULL, CURRENT_TIMESTAMP),
(29, 4, 'COOK', 3, '制姜葱油', '食用油15毫升小火加热至微微冒烟，浇在姜葱蓉上激出香味，加生抽10毫升拌匀成蘸料。', '/images/steps/phase-cook-3.jpg', 60, '小火', CURRENT_TIMESTAMP),
(30, 4, 'PLATE', 1, '斩件摆盘', '鸡沥干后斩成均匀块状，按原鸡形码盘，姜葱蘸料碟置于盘边。', '/images/recipes/baiqie-ji.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱5：松鼠桂鱼（8 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(31, 5, 'PREPARE', 1, '备鱼调汁', '鳜鱼1条约600克备好；调糖醋汁：白糖40克、醋30毫升、西红柿50克切碎、清水60毫升、淀粉8克拌匀备用。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(32, 5, 'WASH', 1, '清洗鳜鱼', '鳜鱼去鳞、去鳃、去内脏，流水冲净腹腔黑膜与血水，加料酒10毫升、盐2克涂抹鱼身去腥。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(33, 5, 'CUT', 1, '去骨剞花刀', '从鳃后下刀沿脊骨片下两侧鱼肉（尾部相连），剔除胸刺，在鱼肉面斜剞菱形花刀，深至鱼皮但不切破。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(34, 5, 'CUT', 2, '拍粉抖粉', '鱼肉与鱼头均匀拍上干淀粉50克，提起鱼尾抖去余粉，使每条花刀缝隙都沾粉且不结块。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(35, 5, 'COOK', 1, '初炸定型', '食用油烧至七成热，手提鱼尾让花刀面朝下淋油定型后整体入锅，炸3分钟至花刀根根绽开。', '/images/steps/phase-cook-1.jpg', 180, '大火', CURRENT_TIMESTAMP),
(36, 5, 'COOK', 2, '复炸上色', '油温回升后复炸约60秒至通体金黄酥脆，捞出沥油，摆入长盘呈昂首翘尾状。', '/images/steps/phase-cook-2.jpg', 60, '大火', CURRENT_TIMESTAMP),
(37, 5, 'COOK', 3, '熬汁浇汁', '锅留底油下糖醋汁，中火熬约90秒至浓稠透亮，趁热从头至尾浇在鱼身上，发出滋啦声响。', '/images/steps/phase-cook-3.jpg', 90, '中火', CURRENT_TIMESTAMP),
(38, 5, 'PLATE', 1, '整形上桌', '微调鱼头鱼尾使其上翘成松鼠回望状，汁亮形挺，立即上桌。', '/images/recipes/songshu-guiyu.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱6：荔枝肉（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(39, 6, 'PREPARE', 1, '腌肉调汁', '猪里脊300克备好，加料酒、盐1克腌10分钟；调汁：白糖25克、醋20毫升、清水40毫升、淀粉5克拌匀。', '/images/steps/phase-prepare.jpg', 600, NULL, CURRENT_TIMESTAMP),
(40, 6, 'WASH', 1, '清洗马蹄', '马蹄100克削皮后流水洗净泥沙，蒜冲洗备用。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(41, 6, 'CUT', 1, '剞十字花刀', '里脊切4厘米见方厚块，表面剞细密十字花刀（深约三分之二），再裹上干淀粉25克；马蹄对半切开。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(42, 6, 'COOK', 1, '炸制成形', '食用油烧至六成热，下肉块炸2分钟，花刀受热卷曲成荔枝状，捞出沥油。', '/images/steps/phase-cook-1.jpg', 120, '中火', CURRENT_TIMESTAMP),
(43, 6, 'COOK', 2, '复炸增脆', '油温升高后复炸约60秒至表面金红酥脆，捞出控油。', '/images/steps/phase-cook-2.jpg', 60, '大火', CURRENT_TIMESTAMP),
(44, 6, 'COOK', 3, '熬汁合炒', '锅留底油爆香蒜片，倒入调味汁大火熬浓，下马蹄与荔枝肉块快速翻匀，约90秒收汁亮芡。', '/images/steps/phase-cook-3.jpg', 90, '大火', CURRENT_TIMESTAMP),
(45, 6, 'PLATE', 1, '装盘出菜', '盛入白瓷盘，红亮的荔枝肉间点缀雪白马蹄，色泽如一盘新摘荔枝。', '/images/recipes/lizhi-rou.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱7：西湖醋鱼（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(46, 7, 'PREPARE', 1, '备鱼调汁', '草鱼1条约700克备好；调汁：白糖20克、醋40毫升、生抽15毫升、清水100毫升；姜20克备用；淀粉10克加水调成水淀粉。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(47, 7, 'WASH', 1, '清洗草鱼', '草鱼去鳞去鳃去内脏，反复冲净腹腔黑膜与血水，加料酒10毫升涂抹去腥。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(48, 7, 'CUT', 1, '雌雄片改刀', '草鱼从尾部下刀一剖为二，带脊骨一侧在背肉厚处划一长刀（牡丹片），便于受热均匀；姜切细末。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(49, 7, 'COOK', 1, '氽煮断生', '宽水烧开后下鱼（皮朝上），再沸后转小火加盖氽3分钟，以筷子能轻松插入鱼背最厚处为准，捞出装盘。', '/images/steps/phase-cook-1.jpg', 180, '小火', CURRENT_TIMESTAMP),
(50, 7, 'COOK', 2, '熬制醋汁', '取氽鱼原汤150毫升入锅，加入调味汁与一半姜末，中火熬2分钟使酸甜融合。', '/images/steps/phase-cook-2.jpg', 120, '中火', CURRENT_TIMESTAMP),
(51, 7, 'COOK', 3, '勾芡收汁', '淋入水淀粉勾薄芡，小火推匀约60秒至汤汁呈琥珀色米汤芡。', '/images/steps/phase-cook-3.jpg', 60, '小火', CURRENT_TIMESTAMP),
(52, 7, 'PLATE', 1, '浇汁撒姜', '将醋汁均匀浇在鱼身上，撒剩余姜末，趁热上桌，吃的就是那一口似蟹的鲜。', '/images/recipes/xihu-cuyu.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱8：剁椒鱼头（8 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(53, 8, 'PREPARE', 1, '称重备料', '胖头鱼头1个约800克、剁椒100克备好；蒸鱼盘垫两根筷子架空鱼头，利于蒸汽循环。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(54, 8, 'PREPARE', 2, '腌制去腥', '鱼头均匀抹上盐2克与料酒15毫升，腌制10分钟去腥入底味。', '/images/steps/phase-prepare.jpg', 600, NULL, CURRENT_TIMESTAMP),
(55, 8, 'WASH', 1, '清洗鱼头', '鱼头去鳃，流水反复冲净牙缝血水与腹腔黑膜，黑膜是腥味主要来源务必去净。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(56, 8, 'CUT', 1, '开边切配', '鱼头从下颌处对半劈开但背部相连，平摊成蝴蝶状，脊骨厚处划一刀；姜20克切丝、蒜15克切末、大葱切葱花。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(57, 8, 'COOK', 1, '大火蒸制', '鱼头铺姜丝、盖满剁椒与一半蒜末，水沸后入蒸锅，大火足汽蒸12分钟。', '/images/steps/phase-cook-1.jpg', 720, '大火', CURRENT_TIMESTAMP),
(58, 8, 'COOK', 2, '关火虚蒸', '时间到后不开盖，关火虚蒸3分钟，让余温把鱼脑部位焖透。', '/images/steps/phase-cook-2.jpg', 180, NULL, CURRENT_TIMESTAMP),
(59, 8, 'COOK', 3, '热油激香', '撒葱花与剩余蒜末，食用油30毫升烧至冒烟，趁热浇淋在剁椒上激出香气，约60秒完成。', '/images/steps/phase-cook-3.jpg', 60, '大火', CURRENT_TIMESTAMP),
(60, 8, 'PLATE', 1, '原盘上桌', '倒掉盘底多余腥水后原盘上桌，红椒白肉热气腾腾，配一份手工面拌汤汁最佳。', '/images/recipes/duojiao-yutou.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱9：黄山臭鳜鱼（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(61, 9, 'PREPARE', 1, '备鱼回温', '腌制好的臭鳜鱼1条约600克提前20分钟回至室温；五花肉50克、豆瓣酱15克、姜蒜备好。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(62, 9, 'WASH', 1, '冲洗表面', '臭鳜鱼轻轻冲洗表面盐渍与黏液（不可久泡以免流失风味），厨房纸吸干水分防煎时溅油。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(63, 9, 'CUT', 1, '改刀切配', '鱼身两面各剞3条一字刀便于入味；五花肉切0.5厘米小丁；姜15克切片、蒜10克拍松、干辣椒剪段。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(64, 9, 'COOK', 1, '煎鱼定香', '食用油30毫升润锅，下鱼中火两面各煎2分钟至金黄微焦，盛出备用。', '/images/steps/phase-cook-1.jpg', 240, '中火', CURRENT_TIMESTAMP),
(65, 9, 'COOK', 2, '小火烧制', '余油煸香五花肉丁出油，下豆瓣酱、姜蒜、干辣椒炒香，加生抽15毫升与热水没过鱼身一半，放回鱼小火烧10分钟，中途晃锅防粘。', '/images/steps/phase-cook-2.jpg', 600, '小火', CURRENT_TIMESTAMP),
(66, 9, 'COOK', 3, '大火收汁', '转大火收汁约2分钟，边收边将汤汁不断淋在鱼身，至汁浓亮油。', '/images/steps/phase-cook-3.jpg', 120, '大火', CURRENT_TIMESTAMP),
(67, 9, 'PLATE', 1, '装盘淋汁', '整鱼滑入长盘，浇上锅中浓汁与肉丁，鱼肉呈蒜瓣状即为火候到位。', '/images/recipes/chou-guiyu.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱10：低卡鸡胸时蔬沙拉（6 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(68, 10, 'PREPARE', 1, '称重备料', '鸡胸肉150克、西兰花100克、生菜80克、西红柿80克、黄瓜80克、鸡蛋1个备好；调油醋汁：食用油5毫升、醋10毫升、盐1克摇匀。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(69, 10, 'WASH', 1, '洗净时蔬', '生菜逐叶掰开冷水浸洗后甩干；西兰花、西红柿、黄瓜流水冲净；西兰花可加少许盐浸泡5分钟去虫卵。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(70, 10, 'CUT', 1, '切配食材', '西兰花掰成一口大小的小朵；黄瓜切薄片；西红柿切月牙角；生菜手撕成大片避免刀切氧化。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(71, 10, 'COOK', 1, '水煮鸡胸与蛋', '冷水下鸡胸肉与鸡蛋，加料酒5毫升，水开后转中火煮8分钟关火，鸡胸在汤中浸5分钟后捞出，鸡蛋过凉剥壳。', '/images/steps/phase-cook-1.jpg', 480, '中火', CURRENT_TIMESTAMP),
(72, 10, 'COOK', 2, '焯烫西兰花', '另起锅水开加盐1克，下西兰花大火焯90秒保持翠绿脆嫩，捞出过凉沥干。', '/images/steps/phase-cook-2.jpg', 90, '大火', CURRENT_TIMESTAMP),
(73, 10, 'PLATE', 1, '拌制装盘', '鸡胸顺纹撕成细丝，鸡蛋对半切开，与所有时蔬在大碗中混合，淋入油醋汁轻拌均匀，装入浅口沙拉碗。', '/images/recipes/jixiong-shala.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱11：红烧肉（8 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(74, 11, 'PREPARE', 1, '称重备料', '带皮五花肉500克、冰糖30克、八角2颗、姜15克、大葱20克备好；生抽20毫升、老抽10毫升、料酒30毫升量好待用。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(75, 11, 'WASH', 1, '冲洗五花肉', '五花肉整条流水冲净表面血水，猪皮上残留的毛用镊子拔净或火燎后刮洗。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(76, 11, 'CUT', 1, '切块切配', '五花肉切2.5厘米见方的大块（炖后缩水，宁大勿小）；姜切厚片，大葱切段。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(77, 11, 'COOK', 1, '冷水焯肉', '肉块冷水下锅，加料酒15毫升与姜片2片，水开后撇净浮沫再煮3分钟，捞出温水冲洗沥干。', '/images/steps/phase-cook-1.jpg', 600, '中火', CURRENT_TIMESTAMP),
(78, 11, 'COOK', 2, '炒制糖色', '锅中放食用油10毫升与冰糖，小火慢慢熬至冰糖融化转琥珀色冒小泡，立刻下肉块快速翻炒挂色。', '/images/steps/phase-cook-2.jpg', 120, '小火', CURRENT_TIMESTAMP),
(79, 11, 'COOK', 3, '小火慢炖', '加入八角、姜片、葱段炒香，烹入料酒与生抽老抽，倒热水没过肉面，大火烧开后转小火加盖炖40分钟。', '/images/steps/phase-cook-3.jpg', 2400, '小火', CURRENT_TIMESTAMP),
(80, 11, 'COOK', 4, '大火收汁', '开盖转大火收汁约5分钟，不断翻动让每块肉裹满酱汁，至汤汁浓稠发亮、能挂在勺背。', '/images/steps/phase-cook-1.jpg', 300, '大火', CURRENT_TIMESTAMP),
(81, 11, 'PLATE', 1, '装碗上桌', '夹出八角葱段不要，肉块码入深碗浇上余汁，油亮红润、颤巍巍冒着热气即成。', '/images/recipes/hongshao-rou.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱12：番茄炒蛋（6 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(82, 12, 'PREPARE', 1, '打蛋备料', '鸡蛋3个磕入碗中，加盐1克、清水5毫升打散至蛋液均匀起泡；白糖5克备在手边。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(83, 12, 'WASH', 1, '清洗西红柿', '西红柿300克流水洗净，蒂部挖除；喜欢细腻口感可在顶部划十字、开水烫30秒后撕去外皮。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(84, 12, 'CUT', 1, '切块切葱', '西红柿切成一口大小的滚刀块；大葱10克切成葱花备用。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(85, 12, 'COOK', 1, '滑炒鸡蛋', '热锅倒食用油15毫升，油温五成热倒入蛋液，凝固成形后用铲子划成大块，七成熟立即盛出。', '/images/steps/phase-cook-1.jpg', 60, '中火', CURRENT_TIMESTAMP),
(86, 12, 'COOK', 2, '炒茄合蛋', '锅留底油下西红柿块，中火炒2分钟至出沙出汁，加白糖与盐1克调味，倒回鸡蛋轻翻裹匀汤汁。', '/images/steps/phase-cook-2.jpg', 120, '中火', CURRENT_TIMESTAMP),
(87, 12, 'PLATE', 1, '撒葱出锅', '关火撒葱花，连汁带料盛入浅盘，红黄相间、汁水饱满，浇饭一绝。', '/images/recipes/fanqie-chaodan.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱13：鱼香肉丝（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(88, 13, 'PREPARE', 1, '腌肉调汁', '猪里脊200克切丝，加料酒5毫升、盐1克、淀粉5克抓匀腌10分钟；调鱼香汁：白糖15克、醋15毫升、生抽10毫升、淀粉5克、清水30毫升拌匀。', '/images/steps/phase-prepare.jpg', 600, NULL, CURRENT_TIMESTAMP),
(89, 13, 'WASH', 1, '泡发清洗', '木耳50克温水泡发20分钟后洗净杂质；竹笋、胡萝卜、青椒流水冲净。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(90, 13, 'CUT', 1, '统一切丝', '木耳、竹笋80克、胡萝卜50克、青椒40克全部切成与肉丝粗细一致的细丝；蒜10克切末。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(91, 13, 'COOK', 1, '滑炒肉丝', '热锅倒食用油20毫升，五成油温下肉丝快速滑散，炒至变色立即盛出。', '/images/steps/phase-cook-1.jpg', 90, '大火', CURRENT_TIMESTAMP),
(92, 13, 'COOK', 2, '炒香酱料', '锅留底油，下豆瓣酱15克与蒜末，中火炒出红油与香气，约60秒。', '/images/steps/phase-cook-2.jpg', 60, '中火', CURRENT_TIMESTAMP),
(93, 13, 'COOK', 3, '合炒淋汁', '下笋丝、胡萝卜丝、木耳丝大火翻炒1分钟，倒回肉丝与青椒丝，淋入鱼香汁快速翻匀至收汁亮芡。', '/images/steps/phase-cook-3.jpg', 90, '大火', CURRENT_TIMESTAMP),
(94, 13, 'PLATE', 1, '装盘出菜', '趁热装盘，红亮的酱汁均匀挂在五彩丝缕上，配米饭立刻开动。', '/images/recipes/yuxiang-rousi.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱14：回锅肉（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(95, 14, 'PREPARE', 1, '称重备料', '带皮五花肉350克、蒜苗100克、豆瓣酱20克、青椒30克备好；姜10克切片。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(96, 14, 'WASH', 1, '清洗食材', '五花肉冲净血水；蒜苗剥去老皮、青椒去蒂，流水洗净沥干。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(97, 14, 'COOK', 1, '整块煮肉', '五花肉整块冷水下锅，加姜片与料酒15毫升，煮20分钟至筷子能插透，捞出晾凉。', '/images/steps/phase-cook-1.jpg', 1200, '中火', CURRENT_TIMESTAMP),
(98, 14, 'CUT', 1, '切片切段', '晾凉的五花肉切2毫米薄片（越薄越易起灯盏窝）；蒜苗斜刀切段、秆叶分开；青椒切菱形块。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(99, 14, 'COOK', 2, '煸出灯盏窝', '热锅少油，下肉片中火煸炒2分钟，至肉片吐油、边缘卷曲成灯盏窝状。', '/images/steps/phase-cook-2.jpg', 120, '中火', CURRENT_TIMESTAMP),
(100, 14, 'COOK', 3, '下酱合炒', '拨开肉片下豆瓣酱炒出红油，加生抽5毫升，先下蒜苗秆与青椒炒30秒，再下蒜苗叶大火翻匀断生即关火。', '/images/steps/phase-cook-3.jpg', 90, '大火', CURRENT_TIMESTAMP),
(101, 14, 'PLATE', 1, '起锅装盘', '连油带肉盛入白瓷盘，红亮肉片间蒜苗青翠，锅气十足趁热吃。', '/images/recipes/huiguo-rou.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱15：酸菜鱼（8 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(102, 15, 'PREPARE', 1, '片鱼腌制', '草鱼700克去骨片成3毫米蝴蝶片，鱼骨斩段；鱼片加盐1克、料酒15毫升、蛋清1个、淀粉10克抓匀腌10分钟。', '/images/steps/phase-prepare.jpg', 600, NULL, CURRENT_TIMESTAMP),
(103, 15, 'WASH', 1, '清洗酸菜', '酸菜200克流水冲洗两遍挤干水分（去除多余盐分与酸涩味）；姜蒜冲净。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(104, 15, 'CUT', 1, '切段切配', '酸菜切3厘米段；姜15克切片、蒜15克拍松；干辣椒5克剪段备用。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(105, 15, 'COOK', 1, '煸炒酸菜', '锅中倒食用油20毫升，下姜蒜爆香后加酸菜中火煸炒90秒，炒出乳酸香气。', '/images/steps/phase-cook-1.jpg', 90, '中火', CURRENT_TIMESTAMP),
(106, 15, 'COOK', 2, '熬制汤底', '下鱼骨煎至微黄，冲入开水800毫升，大火滚煮5分钟至汤色奶白，加盐2克调味。', '/images/steps/phase-cook-2.jpg', 300, '大火', CURRENT_TIMESTAMP),
(107, 15, 'COOK', 3, '汆烫鱼片', '转小火保持微沸，逐片下入鱼片不要翻动，轻晃锅让鱼片自然定型，煮60秒断生立即关火。', '/images/steps/phase-cook-3.jpg', 60, '小火', CURRENT_TIMESTAMP),
(108, 15, 'COOK', 4, '热油激香', '鱼片连汤倒入深盆，铺花椒3克与干辣椒段，食用油20毫升烧至冒烟浇上，滋啦一声香气四起。', '/images/steps/phase-cook-1.jpg', 30, '大火', CURRENT_TIMESTAMP),
(109, 15, 'PLATE', 1, '撒葱上桌', '撒上葱花即可端盆上桌，先喝汤后吃鱼，酸辣开胃。', '/images/recipes/suancai-yu.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱16：扬州炒饭（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(110, 16, 'PREPARE', 1, '备饭打蛋', '隔夜米饭300克提前取出回温并用手打散结块；鸡蛋2个打散；虾仁50克用料酒5毫升腌5分钟。', '/images/steps/phase-prepare.jpg', 300, NULL, CURRENT_TIMESTAMP),
(111, 16, 'WASH', 1, '清洗配料', '虾仁挑去虾线冲净；豌豆30克、胡萝卜30克流水洗净。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(112, 16, 'CUT', 1, '切丁备料', '火腿50克、胡萝卜切成豌豆大小的细丁；大葱10克切葱花。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(113, 16, 'COOK', 1, '炒蛋打底', '热锅倒食用油15毫升，倒入蛋液快速划散成细碎金黄的蛋花，盛出备用。', '/images/steps/phase-cook-1.jpg', 45, '中火', CURRENT_TIMESTAMP),
(114, 16, 'COOK', 2, '大火炒饭', '锅中补油10毫升，下米饭大火翻炒压散，炒约2分钟至米粒在锅中跳动、粒粒分明。', '/images/steps/phase-cook-2.jpg', 120, '大火', CURRENT_TIMESTAMP),
(115, 16, 'COOK', 3, '下料合炒', '下虾仁、火腿丁、胡萝卜丁、豌豆与蛋花，加盐2克，大火再炒90秒至虾仁卷曲变红、配料均匀。', '/images/steps/phase-cook-3.jpg', 90, '大火', CURRENT_TIMESTAMP),
(116, 16, 'PLATE', 1, '撒葱装碗', '关火撒葱花翻两下，盛入大碗压实后倒扣在盘中成小山状，金黄油亮开吃。', '/images/recipes/yangzhou-chaofan.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱17：清蒸鲈鱼（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(117, 17, 'PREPARE', 1, '备鱼回温', '鲜活鲈鱼1条约600克宰杀后提前15分钟回至室温；调豉汁：生抽20毫升加清水10毫升、白糖少许拌匀。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(118, 17, 'WASH', 1, '清洗去腥', '鲈鱼去鳞去鳃去内脏，重点冲净腹腔黑膜，用料酒15毫升里外涂抹去腥，沥干水分。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(119, 17, 'CUT', 1, '改刀切丝', '鱼身两面各划两道斜刀便于蒸透；姜20克一半切片塞入鱼腹、一半切细丝；大葱30克切丝泡水卷曲。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(120, 17, 'COOK', 1, '大火足汽蒸', '盘底垫两根筷子架空鱼身，水沸后入蒸锅，大火足汽蒸8分钟（600克左右的鱼为准）。', '/images/steps/phase-cook-1.jpg', 480, '大火', CURRENT_TIMESTAMP),
(121, 17, 'COOK', 2, '关火虚蒸', '关火不开盖虚蒸2分钟，让余温把鱼骨缝焖熟，鱼肉离骨而不柴。', '/images/steps/phase-cook-2.jpg', 120, NULL, CURRENT_TIMESTAMP),
(122, 17, 'COOK', 3, '淋油浇汁', '倒掉盘中腥水，铺姜丝葱丝，食用油20毫升烧至冒烟浇在丝上激香，沿盘边淋入豉汁。', '/images/steps/phase-cook-3.jpg', 60, '大火', CURRENT_TIMESTAMP),
(123, 17, 'PLATE', 1, '原盘上桌', '原盘上桌，筷子轻拨鱼肉如蒜瓣离骨，蘸着盘底豉汁吃最鲜。', '/images/recipes/qingzheng-luyu.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱18：可乐鸡翅（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(124, 18, 'PREPARE', 1, '划刀腌制', '鸡翅中500克两面各划两刀，加料酒15毫升、姜片3片抓匀腌制10分钟去腥。', '/images/steps/phase-prepare.jpg', 600, NULL, CURRENT_TIMESTAMP),
(125, 18, 'WASH', 1, '冲洗沥干', '腌好的鸡翅冲净浮沫，厨房纸吸干表面水分，煎时才不溅油、上色均匀。', '/images/steps/phase-wash.jpg', 0, NULL, CURRENT_TIMESTAMP),
(126, 18, 'CUT', 1, '切配姜片', '姜15克切片备用；如喜欢更浓郁可再备葱段少许。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(127, 18, 'COOK', 1, '煎至金黄', '锅中倒食用油10毫升，鸡翅皮朝下中火煎3分钟，翻面再煎至两面金黄微焦。', '/images/steps/phase-cook-1.jpg', 180, '中火', CURRENT_TIMESTAMP),
(128, 18, 'COOK', 2, '可乐焖煮', '下姜片，倒入可乐330毫升与生抽20毫升没过鸡翅，大火烧开后转小火加盖焖15分钟。', '/images/steps/phase-cook-2.jpg', 900, '小火', CURRENT_TIMESTAMP),
(129, 18, 'COOK', 3, '大火收汁', '开盖转大火收汁约3分钟，勤翻面防糊底，至汤汁浓稠油亮、能挂在鸡翅表面。', '/images/steps/phase-cook-3.jpg', 180, '大火', CURRENT_TIMESTAMP),
(130, 18, 'PLATE', 1, '摆盘出餐', '鸡翅围圈码入彩盘，中间浇上浓汁，酱红油亮、轻轻一咬就脱骨。', '/images/recipes/kele-jichi.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱19：蒜蓉西兰花（6 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(131, 19, 'PREPARE', 1, '备料切蒜', '西兰花400克、蒜20克备好；蒜切成米粒大小的蒜末，分成两份。', '/images/steps/phase-prepare.jpg', 0, NULL, CURRENT_TIMESTAMP),
(132, 19, 'WASH', 1, '盐水浸洗', '西兰花倒置于淡盐水中浸泡5分钟逼出菜虫，再流水冲净沥干。', '/images/steps/phase-wash.jpg', 300, NULL, CURRENT_TIMESTAMP),
(133, 19, 'CUT', 1, '掰朵削梗', '西兰花掰成一口大小的小朵，粗梗削皮后切片同炒不浪费。', '/images/steps/phase-cut.jpg', 0, NULL, CURRENT_TIMESTAMP),
(134, 19, 'COOK', 1, '焯水锁绿', '锅中水开加盐2克与几滴油，下西兰花大火焯90秒，捞出过凉水保持翠绿脆嫩。', '/images/steps/phase-cook-1.jpg', 90, '大火', CURRENT_TIMESTAMP),
(135, 19, 'COOK', 2, '爆香快炒', '热锅倒食用油15毫升，下一半蒜末小火爆香，倒入西兰花转大火快炒30秒，加盐1克与剩余蒜末翻匀即关火。', '/images/steps/phase-cook-2.jpg', 60, '大火', CURRENT_TIMESTAMP),
(136, 19, 'PLATE', 1, '装盘上桌', '堆入白瓷盘，翠绿油亮、蒜香扑鼻，趁热吃口感最脆。', '/images/recipes/suanrong-xilanhua.jpg', 0, NULL, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 5. 菜谱食材关联 recipe_ingredient（144 条；名称与食材库一致）
-- ---------------------------------------------------------------
-- 菜谱1：宫保鸡丁
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(1, 1, 1,  '鸡胸肉', 150.00, '克',   1, CURRENT_TIMESTAMP),
(2, 1, 14, '黄瓜',   80.00,  '克',   0, CURRENT_TIMESTAMP),
(3, 1, 15, '胡萝卜', 50.00,  '克',   0, CURRENT_TIMESTAMP),
(4, 1, 26, '大葱',   30.00,  '克',   0, CURRENT_TIMESTAMP),
(5, 1, 21, '干辣椒', 8.00,   '克',   1, CURRENT_TIMESTAMP),
(6, 1, 24, '花椒',   3.00,   '克',   1, CURRENT_TIMESTAMP),
(7, 1, 25, '花生米', 30.00,  '克',   1, CURRENT_TIMESTAMP),
(8, 1, 30, '醋',     20.00,  '毫升', 1, CURRENT_TIMESTAMP),
(9, 1, 31, '白糖',   10.00,  '克',   1, CURRENT_TIMESTAMP);
-- 菜谱2：麻婆豆腐
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(10, 2, 6,  '豆腐',   400.00, '克',   1, CURRENT_TIMESTAMP),
(11, 2, 4,  '猪肉末', 80.00,  '克',   1, CURRENT_TIMESTAMP),
(12, 2, 23, '豆瓣酱', 20.00,  '克',   1, CURRENT_TIMESTAMP),
(13, 2, 24, '花椒',   5.00,   '克',   1, CURRENT_TIMESTAMP),
(14, 2, 26, '大葱',   20.00,  '克',   0, CURRENT_TIMESTAMP),
(15, 2, 28, '蒜',     10.00,  '克',   0, CURRENT_TIMESTAMP),
(16, 2, 33, '淀粉',   8.00,   '克',   1, CURRENT_TIMESTAMP),
(17, 2, 34, '食用油', 20.00,  '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱3：糖醋里脊
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(18, 3, 3,  '猪里脊', 250.00, '克',   1, CURRENT_TIMESTAMP),
(19, 3, 12, '鸡蛋',   1.00,   '个',   1, CURRENT_TIMESTAMP),
(20, 3, 33, '淀粉',   40.00,  '克',   1, CURRENT_TIMESTAMP),
(21, 3, 31, '白糖',   30.00,  '克',   1, CURRENT_TIMESTAMP),
(22, 3, 30, '醋',     25.00,  '毫升', 1, CURRENT_TIMESTAMP),
(23, 3, 29, '生抽',   10.00,  '毫升', 0, CURRENT_TIMESTAMP),
(24, 3, 34, '食用油', 500.00, '毫升', 1, CURRENT_TIMESTAMP),
(25, 3, 36, '盐',     1.00,   '克',   1, CURRENT_TIMESTAMP);
-- 菜谱4：白切鸡
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(26, 4, 5,  '三黄鸡', 1000.00, '克',   1, CURRENT_TIMESTAMP),
(27, 4, 27, '姜',     30.00,   '克',   1, CURRENT_TIMESTAMP),
(28, 4, 26, '大葱',   30.00,   '克',   1, CURRENT_TIMESTAMP),
(29, 4, 34, '食用油', 15.00,   '毫升', 1, CURRENT_TIMESTAMP),
(30, 4, 36, '盐',     5.00,    '克',   1, CURRENT_TIMESTAMP),
(31, 4, 29, '生抽',   10.00,   '毫升', 0, CURRENT_TIMESTAMP);
-- 菜谱5：松鼠桂鱼
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(32, 5, 8,  '鳜鱼',   600.00, '克',   1, CURRENT_TIMESTAMP),
(33, 5, 33, '淀粉',   60.00,  '克',   1, CURRENT_TIMESTAMP),
(34, 5, 31, '白糖',   40.00,  '克',   1, CURRENT_TIMESTAMP),
(35, 5, 30, '醋',     30.00,  '毫升', 1, CURRENT_TIMESTAMP),
(36, 5, 13, '西红柿', 50.00,  '克',   0, CURRENT_TIMESTAMP),
(37, 5, 34, '食用油', 600.00, '毫升', 1, CURRENT_TIMESTAMP),
(38, 5, 36, '盐',     2.00,   '克',   1, CURRENT_TIMESTAMP),
(39, 5, 35, '料酒',   10.00,  '毫升', 0, CURRENT_TIMESTAMP);
-- 菜谱6：荔枝肉
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(40, 6, 3,  '猪里脊', 300.00, '克',   1, CURRENT_TIMESTAMP),
(41, 6, 20, '马蹄',   100.00, '克',   1, CURRENT_TIMESTAMP),
(42, 6, 31, '白糖',   25.00,  '克',   1, CURRENT_TIMESTAMP),
(43, 6, 30, '醋',     20.00,  '毫升', 1, CURRENT_TIMESTAMP),
(44, 6, 33, '淀粉',   30.00,  '克',   1, CURRENT_TIMESTAMP),
(45, 6, 28, '蒜',     10.00,  '克',   0, CURRENT_TIMESTAMP),
(46, 6, 34, '食用油', 400.00, '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱7：西湖醋鱼
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(47, 7, 9,  '草鱼', 700.00, '克',   1, CURRENT_TIMESTAMP),
(48, 7, 31, '白糖', 20.00,  '克',   1, CURRENT_TIMESTAMP),
(49, 7, 30, '醋',   40.00,  '毫升', 1, CURRENT_TIMESTAMP),
(50, 7, 29, '生抽', 15.00,  '毫升', 1, CURRENT_TIMESTAMP),
(51, 7, 27, '姜',   20.00,  '克',   1, CURRENT_TIMESTAMP),
(52, 7, 33, '淀粉', 10.00,  '克',   1, CURRENT_TIMESTAMP),
(53, 7, 35, '料酒', 10.00,  '毫升', 0, CURRENT_TIMESTAMP);
-- 菜谱8：剁椒鱼头
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(54, 8, 10, '胖头鱼', 800.00, '克',   1, CURRENT_TIMESTAMP),
(55, 8, 22, '剁椒',   100.00, '克',   1, CURRENT_TIMESTAMP),
(56, 8, 27, '姜',     20.00,  '克',   1, CURRENT_TIMESTAMP),
(57, 8, 28, '蒜',     15.00,  '克',   1, CURRENT_TIMESTAMP),
(58, 8, 26, '大葱',   15.00,  '克',   0, CURRENT_TIMESTAMP),
(59, 8, 35, '料酒',   15.00,  '毫升', 1, CURRENT_TIMESTAMP),
(60, 8, 34, '食用油', 30.00,  '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱9：黄山臭鳜鱼
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(61, 9, 8,  '鳜鱼',   600.00, '克',   1, CURRENT_TIMESTAMP),
(62, 9, 2,  '五花肉', 50.00,  '克',   1, CURRENT_TIMESTAMP),
(63, 9, 23, '豆瓣酱', 15.00,  '克',   1, CURRENT_TIMESTAMP),
(64, 9, 27, '姜',     15.00,  '克',   1, CURRENT_TIMESTAMP),
(65, 9, 28, '蒜',     10.00,  '克',   1, CURRENT_TIMESTAMP),
(66, 9, 21, '干辣椒', 5.00,   '克',   0, CURRENT_TIMESTAMP),
(67, 9, 29, '生抽',   15.00,  '毫升', 1, CURRENT_TIMESTAMP),
(68, 9, 34, '食用油', 30.00,  '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱10：低卡鸡胸时蔬沙拉
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(69, 10, 1,  '鸡胸肉', 150.00, '克',   1, CURRENT_TIMESTAMP),
(70, 10, 18, '西兰花', 100.00, '克',   1, CURRENT_TIMESTAMP),
(71, 10, 19, '生菜',   80.00,  '克',   1, CURRENT_TIMESTAMP),
(72, 10, 13, '西红柿', 80.00,  '克',   0, CURRENT_TIMESTAMP),
(73, 10, 14, '黄瓜',   80.00,  '克',   0, CURRENT_TIMESTAMP),
(74, 10, 12, '鸡蛋',   1.00,   '个',   1, CURRENT_TIMESTAMP),
(75, 10, 30, '醋',     10.00,  '毫升', 1, CURRENT_TIMESTAMP),
(76, 10, 34, '食用油', 5.00,   '毫升', 1, CURRENT_TIMESTAMP),
(77, 10, 36, '盐',     1.00,   '克',   1, CURRENT_TIMESTAMP);
-- 菜谱11：红烧肉
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(78, 11, 2,  '五花肉', 500.00, '克',   1, CURRENT_TIMESTAMP),
(79, 11, 48, '冰糖',   30.00,  '克',   1, CURRENT_TIMESTAMP),
(80, 11, 47, '八角',   2.00,   '颗',   1, CURRENT_TIMESTAMP),
(81, 11, 27, '姜',     15.00,  '克',   1, CURRENT_TIMESTAMP),
(82, 11, 26, '大葱',   20.00,  '克',   0, CURRENT_TIMESTAMP),
(83, 11, 29, '生抽',   20.00,  '毫升', 1, CURRENT_TIMESTAMP),
(84, 11, 46, '老抽',   10.00,  '毫升', 1, CURRENT_TIMESTAMP),
(85, 11, 35, '料酒',   30.00,  '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱12：番茄炒蛋
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(86, 12, 13, '西红柿', 300.00, '克',   1, CURRENT_TIMESTAMP),
(87, 12, 12, '鸡蛋',   3.00,   '个',   1, CURRENT_TIMESTAMP),
(88, 12, 26, '大葱',   10.00,  '克',   0, CURRENT_TIMESTAMP),
(89, 12, 31, '白糖',   5.00,   '克',   0, CURRENT_TIMESTAMP),
(90, 12, 36, '盐',     2.00,   '克',   1, CURRENT_TIMESTAMP),
(91, 12, 34, '食用油', 20.00,  '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱13：鱼香肉丝
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(92,  13, 3,  '猪里脊', 200.00, '克',   1, CURRENT_TIMESTAMP),
(93,  13, 37, '木耳',   50.00,  '克',   1, CURRENT_TIMESTAMP),
(94,  13, 38, '竹笋',   80.00,  '克',   0, CURRENT_TIMESTAMP),
(95,  13, 15, '胡萝卜', 50.00,  '克',   0, CURRENT_TIMESTAMP),
(96,  13, 17, '青椒',   40.00,  '克',   0, CURRENT_TIMESTAMP),
(97,  13, 23, '豆瓣酱', 15.00,  '克',   1, CURRENT_TIMESTAMP),
(98,  13, 31, '白糖',   15.00,  '克',   1, CURRENT_TIMESTAMP),
(99,  13, 30, '醋',     15.00,  '毫升', 1, CURRENT_TIMESTAMP),
(100, 13, 33, '淀粉',   10.00,  '克',   1, CURRENT_TIMESTAMP),
(101, 13, 28, '蒜',     10.00,  '克',   1, CURRENT_TIMESTAMP);
-- 菜谱14：回锅肉
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(102, 14, 2,  '五花肉', 350.00, '克',   1, CURRENT_TIMESTAMP),
(103, 14, 39, '蒜苗',   100.00, '克',   1, CURRENT_TIMESTAMP),
(104, 14, 23, '豆瓣酱', 20.00,  '克',   1, CURRENT_TIMESTAMP),
(105, 14, 17, '青椒',   30.00,  '克',   0, CURRENT_TIMESTAMP),
(106, 14, 27, '姜',     10.00,  '克',   1, CURRENT_TIMESTAMP),
(107, 14, 35, '料酒',   15.00,  '毫升', 1, CURRENT_TIMESTAMP),
(108, 14, 29, '生抽',   5.00,   '毫升', 0, CURRENT_TIMESTAMP),
(109, 14, 34, '食用油', 10.00,  '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱15：酸菜鱼
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(110, 15, 9,  '草鱼',   700.00, '克',   1, CURRENT_TIMESTAMP),
(111, 15, 45, '酸菜',   200.00, '克',   1, CURRENT_TIMESTAMP),
(112, 15, 12, '鸡蛋',   1.00,   '个',   1, CURRENT_TIMESTAMP),
(113, 15, 33, '淀粉',   10.00,  '克',   1, CURRENT_TIMESTAMP),
(114, 15, 24, '花椒',   3.00,   '克',   1, CURRENT_TIMESTAMP),
(115, 15, 21, '干辣椒', 5.00,   '克',   1, CURRENT_TIMESTAMP),
(116, 15, 27, '姜',     15.00,  '克',   1, CURRENT_TIMESTAMP),
(117, 15, 28, '蒜',     15.00,  '克',   1, CURRENT_TIMESTAMP),
(118, 15, 35, '料酒',   15.00,  '毫升', 1, CURRENT_TIMESTAMP),
(119, 15, 34, '食用油', 40.00,  '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱16：扬州炒饭
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(120, 16, 42, '米饭',   300.00, '克',   1, CURRENT_TIMESTAMP),
(121, 16, 12, '鸡蛋',   2.00,   '个',   1, CURRENT_TIMESTAMP),
(122, 16, 11, '虾仁',   50.00,  '克',   1, CURRENT_TIMESTAMP),
(123, 16, 43, '火腿',   50.00,  '克',   0, CURRENT_TIMESTAMP),
(124, 16, 44, '豌豆',   30.00,  '克',   0, CURRENT_TIMESTAMP),
(125, 16, 15, '胡萝卜', 30.00,  '克',   0, CURRENT_TIMESTAMP),
(126, 16, 26, '大葱',   10.00,  '克',   0, CURRENT_TIMESTAMP),
(127, 16, 34, '食用油', 25.00,  '毫升', 1, CURRENT_TIMESTAMP),
(128, 16, 36, '盐',     2.00,   '克',   1, CURRENT_TIMESTAMP);
-- 菜谱17：清蒸鲈鱼
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(129, 17, 7,  '鲈鱼',   600.00, '克',   1, CURRENT_TIMESTAMP),
(130, 17, 27, '姜',     20.00,  '克',   1, CURRENT_TIMESTAMP),
(131, 17, 26, '大葱',   30.00,  '克',   1, CURRENT_TIMESTAMP),
(132, 17, 29, '生抽',   20.00,  '毫升', 1, CURRENT_TIMESTAMP),
(133, 17, 35, '料酒',   15.00,  '毫升', 1, CURRENT_TIMESTAMP),
(134, 17, 34, '食用油', 20.00,  '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱18：可乐鸡翅
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(135, 18, 40, '鸡翅',   500.00, '克',   1, CURRENT_TIMESTAMP),
(136, 18, 41, '可乐',   330.00, '毫升', 1, CURRENT_TIMESTAMP),
(137, 18, 27, '姜',     15.00,  '克',   1, CURRENT_TIMESTAMP),
(138, 18, 29, '生抽',   20.00,  '毫升', 1, CURRENT_TIMESTAMP),
(139, 18, 35, '料酒',   15.00,  '毫升', 1, CURRENT_TIMESTAMP),
(140, 18, 34, '食用油', 10.00,  '毫升', 1, CURRENT_TIMESTAMP);
-- 菜谱19：蒜蓉西兰花
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, ingredient_name, amount, unit, is_essential, created_at) VALUES
(141, 19, 18, '西兰花', 400.00, '克',   1, CURRENT_TIMESTAMP),
(142, 19, 28, '蒜',     20.00,  '克',   1, CURRENT_TIMESTAMP),
(143, 19, 34, '食用油', 15.00,  '毫升', 1, CURRENT_TIMESTAMP),
(144, 19, 36, '盐',     3.00,   '克',   1, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 6. 菜谱标签关联 recipe_tag_relation（71 条；1适宜 2慎用）
-- ---------------------------------------------------------------
INSERT INTO recipe_tag_relation (id, recipe_id, tag_id, relation_type) VALUES
-- 宫保鸡丁：适宜 增肌期/健身后/下饭菜；慎用 控糖人群（碗汁含糖）
(1,  1,  2,  1),
(2,  1,  15, 1),
(3,  1,  13, 1),
(4,  1,  3,  2),
-- 麻婆豆腐：适宜 下饭菜/快手菜/增肌期；慎用 孕妇（麻辣刺激）
(5,  2,  13, 1),
(6,  2,  12, 1),
(7,  2,  2,  1),
(8,  2,  7,  2),
-- 糖醋里脊：适宜 儿童适宜/下饭菜；慎用 控糖人群/减脂期（高糖高油）
(9,  3,  5,  1),
(10, 3,  13, 1),
(11, 3,  3,  2),
(12, 3,  1,  2),
-- 白切鸡：适宜 高蛋白/老人适宜/增肌期
(13, 4,  9,  1),
(14, 4,  6,  1),
(15, 4,  2,  1),
-- 松鼠桂鱼：适宜 儿童适宜/下饭菜/宴客菜；慎用 痛风慎用/控糖人群
(16, 5,  5,  1),
(17, 5,  13, 1),
(18, 5,  8,  2),
(19, 5,  3,  2),
-- 荔枝肉：适宜 下饭菜/儿童适宜；慎用 控糖人群
(20, 6,  13, 1),
(21, 6,  5,  1),
(22, 6,  3,  2),
-- 西湖醋鱼：适宜 低卡/老人适宜/减脂期；慎用 痛风慎用
(23, 7,  10, 1),
(24, 7,  6,  1),
(25, 7,  1,  1),
(26, 7,  8,  2),
-- 剁椒鱼头：适宜 下饭菜/高蛋白；慎用 痛风慎用/孕妇慎用
(27, 8,  13, 1),
(28, 8,  9,  1),
(29, 8,  8,  2),
(30, 8,  7,  2),
-- 黄山臭鳜鱼：适宜 下饭菜/高蛋白；慎用 痛风慎用
(31, 9,  13, 1),
(32, 9,  9,  1),
(33, 9,  8,  2),
-- 低卡鸡胸时蔬沙拉：适宜 减脂期/健身后/低卡/高蛋白
(34, 10, 1,  1),
(35, 10, 15, 1),
(36, 10, 10, 1),
(37, 10, 9,  1),
-- 红烧肉：适宜 下饭菜/宴客菜；慎用 减脂期/控糖人群
(38, 11, 13, 1),
(39, 11, 18, 1),
(40, 11, 1,  2),
(41, 11, 3,  2),
-- 番茄炒蛋：适宜 快手菜/儿童适宜/一人食/老人适宜
(42, 12, 12, 1),
(43, 12, 5,  1),
(44, 12, 17, 1),
(45, 12, 6,  1),
-- 鱼香肉丝：适宜 下饭菜/快手菜；慎用 控糖人群（鱼香汁含糖）
(46, 13, 13, 1),
(47, 13, 12, 1),
(48, 13, 3,  2),
-- 回锅肉：适宜 下饭菜；慎用 减脂期（高脂）
(49, 14, 13, 1),
(50, 14, 1,  2),
-- 酸菜鱼：适宜 下饭菜/高蛋白；慎用 痛风慎用/孕妇慎用（辛辣）
(51, 15, 13, 1),
(52, 15, 9,  1),
(53, 15, 8,  2),
(54, 15, 7,  2),
-- 扬州炒饭：适宜 快手菜/一人食/儿童适宜；慎用 减脂期（主食油炒）
(55, 16, 12, 1),
(56, 16, 17, 1),
(57, 16, 5,  1),
(58, 16, 1,  2),
-- 清蒸鲈鱼：适宜 低卡/高蛋白/老人适宜/减脂期；慎用 痛风慎用
(59, 17, 10, 1),
(60, 17, 9,  1),
(61, 17, 6,  1),
(62, 17, 1,  1),
(63, 17, 8,  2),
-- 可乐鸡翅：适宜 儿童适宜/快手菜/下饭菜；慎用 控糖人群（可乐含糖）
(64, 18, 5,  1),
(65, 18, 12, 1),
(66, 18, 13, 1),
(67, 18, 3,  2),
-- 蒜蓉西兰花：适宜 低卡/素食者/减脂期/快手菜
(68, 19, 10, 1),
(69, 19, 4,  1),
(70, 19, 1,  1),
(71, 19, 12, 1);

-- ---------------------------------------------------------------
-- 7. 智能食材替换规则 recipe_substitute_rule（11 条）
-- 场景：减脂/控糖/素食/低嘌呤
-- ---------------------------------------------------------------
INSERT INTO recipe_substitute_rule (id, source_ingredient_id, target_ingredient_id, scene, reason, created_at) VALUES
(1,  2,  1,  '减脂',   '同等饱腹感下每100g减少约435kcal', CURRENT_TIMESTAMP),
(2,  31, 32, '控糖',   '木糖醇升糖指数远低于蔗糖，几乎不引起血糖波动，适合控糖人群替代白糖', CURRENT_TIMESTAMP),
(3,  4,  6,  '素食',   '豆腐提供优质植物蛋白，口感软嫩，是肉末的经典素食替代', CURRENT_TIMESTAMP),
(4,  11, 1,  '低嘌呤', '鸡胸肉嘌呤含量明显低于虾仁，对痛风人群更友好且蛋白质更高', CURRENT_TIMESTAMP),
(5,  2,  6,  '素食',   '豆腐脂肪含量低且富含植物蛋白，可替代五花肉实现素食改造', CURRENT_TIMESTAMP),
(6,  3,  1,  '减脂',   '鸡胸肉脂肪更低、蛋白更高，每100g减少约22kcal', CURRENT_TIMESTAMP),
(7,  31, 32, '减脂',   '木糖醇热量约为蔗糖的60%，减脂期可有效减少精制糖摄入', CURRENT_TIMESTAMP),
(8,  48, 32, '控糖',   '冰糖与蔗糖升糖相近，控糖时可用木糖醇替代炒糖色（火候需更小心，更易焦）', CURRENT_TIMESTAMP),
(9,  43, 1,  '减脂',   '鸡胸肉替代火腿丁，每100g减少约200kcal并大幅降低钠摄入', CURRENT_TIMESTAMP),
(10, 40, 1,  '减脂',   '鸡胸肉脂肪远低于带皮鸡翅，搭配无糖可乐可把这道菜改造成轻负担版本', CURRENT_TIMESTAMP),
(11, 2,  3,  '减脂',   '猪里脊脂肪不到五花肉的六分之一，回锅肉可改做瘦版回锅肉', CURRENT_TIMESTAMP);

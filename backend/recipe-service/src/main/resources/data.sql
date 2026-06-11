-- =====================================================================
-- 食研社 recipe-service dev 种子数据（H2 MySQL 模式，启动自动执行）
-- 说明：纯 INSERT；显式指定主键 id；日期列统一 CURRENT_TIMESTAMP
-- 表顺序：食材库 -> 标签 -> 菜谱 -> 步骤 -> 菜谱食材 -> 标签关联 -> 替换规则
-- =====================================================================

-- ---------------------------------------------------------------
-- 1. 基础食材库 recipe_ingredient_lib（36 条）
-- 分类：肉类/水产/蛋类/蔬菜/豆制品/坚果/调料/油脂
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
(36, '盐',     '调料', 0.00,   0.00,  0.00,  0.00,  NULL, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 2. 标签字典 recipe_tag（16 条；tag_type：1人群 2功效 3场景）
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
(16, '生理期友好', 3, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 3. 菜谱主表 recipe_info（10 道；status=1 上架，source_type=1 官方）
-- 菜系：1川 2鲁 3粤 4苏 5闽 6浙 7湘 8徽 9家常
-- ---------------------------------------------------------------
INSERT INTO recipe_info (id, title, cover_url, cuisine_type, difficulty, total_time_min, servings, calories_kcal, carbs_g, protein_g, fat_g, description, tips, status, view_count, like_count, author_id, source_type, created_at, updated_at) VALUES
(1, '宫保鸡丁', 'https://picsum.photos/seed/recipe1/600/400', 1, 2, 25, 2, 350.00, 15.00, 30.00, 18.00,
 '川菜宫保系的当家花旦，相传得名于清末四川总督丁宝桢的官衔太子少保。鸡丁滑嫩、花生酥香，荔枝口的小酸甜里藏着糊辣香。',
 '干辣椒去籽可防辣手呛喉；碗汁提前调好，下锅后大火快炒不超过90秒，鸡丁才嫩。', 1, 1280, 326, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '麻婆豆腐', 'https://picsum.photos/seed/recipe2/600/400', 1, 1, 20, 2, 280.00, 12.00, 16.00, 19.00,
 '同治年间成都万福桥边陈麻婆的看家菜，麻、辣、烫、香、酥、嫩、鲜、活八字真言，一勺浇在米饭上便是人间至味。',
 '豆腐先用淡盐水焯1分钟可去豆腥且不易碎；勾芡分两次进行，汁更亮更裹味。', 1, 1500, 412, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '糖醋里脊', 'https://picsum.photos/seed/recipe3/600/400', 2, 2, 30, 2, 420.00, 38.00, 22.00, 20.00,
 '鲁菜糖醋技法的入门名作，金黄酥壳裹着嫩肉，糖醋汁亮如琥珀，是无数人童年记忆里的第一道硬菜。',
 '复炸是酥脆的关键：第一遍定型，第二遍升高油温逼出余油；糖醋汁熬至冒大泡再下肉。', 1, 950, 268, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '白切鸡', 'https://picsum.photos/seed/recipe4/600/400', 3, 2, 45, 4, 310.00, 2.00, 28.00, 21.00,
 '粤菜以鸡为尊，无鸡不成宴。白切鸡讲究皮爽肉滑、骨髓带红，一碟姜葱蓉是它最忠实的伴侣。',
 '浸煮全程保持虾眼水（约90度微沸）不翻滚；煮好立刻入冰水，皮才会爽脆收紧。', 1, 860, 240, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '松鼠桂鱼', 'https://picsum.photos/seed/recipe5/600/400', 4, 3, 50, 3, 380.00, 30.00, 26.00, 18.00,
 '苏帮菜的状元郎，乾隆下江南的传说为它添了三分贵气。改刀成菊、入油成松鼠，浇汁时滋啦一声恰似松鼠鸣叫。',
 '剞花刀深至鱼皮但不能切破；拍粉后抖去余粉，炸出的花刀才根根分明。', 1, 530, 188, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '荔枝肉', 'https://picsum.photos/seed/recipe6/600/400', 5, 2, 35, 2, 390.00, 35.00, 20.00, 19.00,
 '闽菜里有名的有荔枝之形而无荔枝之实的巧菜，十字花刀的里脊炸后卷曲如荔枝，配马蹄同烧，酸甜里带着脆爽。',
 '花刀切得越细密，炸后卷曲越像荔枝；马蹄最后下锅，保持脆感。', 1, 410, 132, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '西湖醋鱼', 'https://picsum.photos/seed/recipe7/600/400', 6, 2, 30, 2, 230.00, 12.00, 24.00, 9.00,
 '杭州楼外楼的镇店之宝，宋嫂传艺的典故流传八百年。不用一滴油的氽煮技法，糖醋汁里要吃出蟹味，才算正宗。',
 '草鱼氽煮以筷子能轻松插入鱼背为度，多一分则老；糖醋汁中姜末不可省，去腥提鲜全靠它。', 1, 640, 175, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, '剁椒鱼头', 'https://picsum.photos/seed/recipe8/600/400', 7, 2, 40, 3, 260.00, 6.00, 25.00, 15.00,
 '湘菜的鸿运当头，红艳艳的剁椒铺满胖头鱼头，蒸汽裹着发酵辣香钻进鱼肉的每一丝纹理，配碗面条收尾才算圆满。',
 '鱼头开边后脊骨处划一刀更易蒸透；出锅后淋热油激香是点睛之笔，油温要冒烟。', 1, 880, 295, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, '黄山臭鳜鱼', 'https://picsum.photos/seed/recipe9/600/400', 8, 3, 60, 3, 290.00, 5.00, 27.00, 17.00,
 '徽商沿新安江贩鱼，木桶淡盐水腌出的似臭非臭成就了徽菜头牌。闻着微臭、吃着透鲜，鱼肉呈蒜瓣状才是上品。',
 '煎鱼前用厨房纸吸干表面水分防溅油；烧制时加少许五花肉丁，鱼肉更润更香。', 1, 360, 120, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, '低卡鸡胸时蔬沙拉', 'https://picsum.photos/seed/recipe10/600/400', 9, 1, 15, 1, 210.00, 10.00, 30.00, 6.00,
 '健身人的快手家常担当，水煮鸡胸撕成细丝拌入五彩时蔬，高蛋白低脂肪，一碗吃出轻盈感。',
 '鸡胸煮好后在汤中浸5分钟再捞出，肉质不柴；油醋汁现拌现吃，蔬菜不出水。', 1, 1100, 358, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 4. 菜谱步骤 recipe_step（73 条；五步法 PREPARE/WASH/CUT/COOK/PLATE）
-- ---------------------------------------------------------------
-- 菜谱1：宫保鸡丁（8 步，参考产品需求文档五步法示例）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(1, 1, 'PREPARE', 1, '食材称重与腌制', '鸡胸肉150克切1.5厘米见方小丁，加生抽10毫升、料酒5毫升、淀粉3克抓匀，腌制15分钟入味。', 'https://picsum.photos/seed/step1/400/300', 900, NULL, CURRENT_TIMESTAMP),
(2, 1, 'PREPARE', 2, '调配碗汁', '取小碗，依次加入醋20毫升、白糖10克、生抽10毫升、淀粉5克、清水30毫升，搅拌至白糖完全融化备用。', 'https://picsum.photos/seed/step2/400/300', 0, NULL, CURRENT_TIMESTAMP),
(3, 1, 'WASH', 1, '清洗配菜', '黄瓜80克、胡萝卜50克流水洗净后去皮；大葱剥去外层老皮冲洗干净；花生米拣去坏粒。', 'https://picsum.photos/seed/step3/400/300', 0, NULL, CURRENT_TIMESTAMP),
(4, 1, 'CUT', 1, '改刀切配', '黄瓜、胡萝卜切1厘米见方小丁；大葱取葱白切2厘米葱节；干辣椒8克剪成段并去籽防辣手。', 'https://picsum.photos/seed/step4/400/300', 0, NULL, CURRENT_TIMESTAMP),
(5, 1, 'COOK', 1, '滑炒鸡丁', '热锅倒食用油20毫升，五成油温（木筷插入冒小泡）下腌好的鸡丁滑散，炒约2分钟至表面变白盛出。', 'https://picsum.photos/seed/step5/400/300', 120, '中火', CURRENT_TIMESTAMP),
(6, 1, 'COOK', 2, '爆香翻炒', '利用锅中余油爆香干辣椒段与花椒3克，闻到糊辣香后下葱节、胡萝卜丁翻炒，再倒回鸡丁与黄瓜丁炒匀，约1分钟。', 'https://picsum.photos/seed/step6/400/300', 60, '大火', CURRENT_TIMESTAMP),
(7, 1, 'COOK', 3, '倒汁收汁', '将碗汁搅匀后沿锅边淋入，大火快速翻炒约90秒，至汤汁浓稠发亮、均匀挂在食材表面。', 'https://picsum.photos/seed/step7/400/300', 90, '大火', CURRENT_TIMESTAMP),
(8, 1, 'PLATE', 1, '摆盘出餐', '关火后拌入花生米30克（保持酥脆），盛入深盘中心堆成小山状，趁热上桌。', 'https://picsum.photos/seed/step8/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱2：麻婆豆腐（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(9,  2, 'PREPARE', 1, '称重备料', '嫩豆腐400克、猪肉末80克、豆瓣酱20克、花椒5克分别备好；淀粉8克加清水30毫升调成水淀粉。', 'https://picsum.photos/seed/step9/400/300', 0, NULL, CURRENT_TIMESTAMP),
(10, 2, 'WASH', 1, '清洗辅料', '大葱、蒜流水冲洗干净；豆腐整块用清水轻轻冲淋一遍沥干。', 'https://picsum.photos/seed/step10/400/300', 0, NULL, CURRENT_TIMESTAMP),
(11, 2, 'CUT', 1, '切块切末', '豆腐切2厘米见方小块；大葱20克切葱花；蒜10克切末；豆瓣酱在砧板上剁细更易出红油。', 'https://picsum.photos/seed/step11/400/300', 0, NULL, CURRENT_TIMESTAMP),
(12, 2, 'COOK', 1, '豆腐焯水', '锅中烧水加盐2克，水开后下豆腐块焯1分钟去豆腥并定型，捞出沥水。', 'https://picsum.photos/seed/step12/400/300', 60, '中火', CURRENT_TIMESTAMP),
(13, 2, 'COOK', 2, '炒酥肉末', '锅中倒食用油20毫升，下猪肉末炒散至微微发酥，加入豆瓣酱与蒜末，中火炒出红油约90秒。', 'https://picsum.photos/seed/step13/400/300', 90, '中火', CURRENT_TIMESTAMP),
(14, 2, 'COOK', 3, '烧制勾芡', '加清水200毫升烧开，轻轻推入豆腐块小火烧3分钟，分两次淋入水淀粉勾芡至汤汁浓亮。', 'https://picsum.photos/seed/step14/400/300', 180, '小火', CURRENT_TIMESTAMP),
(15, 2, 'PLATE', 1, '撒料装盘', '盛入深口碗中，趁热撒上现磨花椒粉与葱花，麻香随热气升腾即可上桌。', 'https://picsum.photos/seed/step15/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱3：糖醋里脊（8 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(16, 3, 'PREPARE', 1, '腌制里脊', '猪里脊250克备好，加料酒10毫升、盐1克抓匀腌制10分钟去腥入底味。', 'https://picsum.photos/seed/step16/400/300', 600, NULL, CURRENT_TIMESTAMP),
(17, 3, 'PREPARE', 2, '调糖醋汁与面糊', '糖醋汁：白糖30克、醋25毫升、生抽10毫升、淀粉5克、清水40毫升调匀。面糊：淀粉35克加鸡蛋1个搅成酸奶状稠糊。', 'https://picsum.photos/seed/step17/400/300', 0, NULL, CURRENT_TIMESTAMP),
(18, 3, 'WASH', 1, '清洗沥干', '里脊冲洗后用厨房纸彻底吸干表面水分，挂糊才牢固、炸时不溅油。', 'https://picsum.photos/seed/step18/400/300', 0, NULL, CURRENT_TIMESTAMP),
(19, 3, 'CUT', 1, '切条挂糊', '里脊顺纹切成1.5厘米粗、6厘米长的条，逐条裹满面糊。', 'https://picsum.photos/seed/step19/400/300', 0, NULL, CURRENT_TIMESTAMP),
(20, 3, 'COOK', 1, '初炸定型', '食用油烧至六成热（约180度），逐条下入里脊条炸2分钟至浅黄定型，捞出沥油。', 'https://picsum.photos/seed/step20/400/300', 120, '中火', CURRENT_TIMESTAMP),
(21, 3, 'COOK', 2, '复炸酥脆', '油温升至八成热（油面轻微冒烟），倒回里脊条复炸约60秒至金黄酥脆，迅速捞出。', 'https://picsum.photos/seed/step21/400/300', 60, '大火', CURRENT_TIMESTAMP),
(22, 3, 'COOK', 3, '熬汁裹匀', '锅留底油，倒入糖醋汁中火熬至冒大泡，下炸好的里脊快速颠锅翻匀，约90秒让每条都裹上亮汁。', 'https://picsum.photos/seed/step22/400/300', 90, '中火', CURRENT_TIMESTAMP),
(23, 3, 'PLATE', 1, '装盘点缀', '趁汁未凝快速装盘，码放整齐，可点缀几片黄瓜解腻。', 'https://picsum.photos/seed/step23/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱4：白切鸡（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(24, 4, 'PREPARE', 1, '备鸡回温', '三黄鸡1只约1000克提前30分钟从冰箱取出回至室温，避免冷鸡下锅导致受热不均；同时备姜30克、大葱30克。', 'https://picsum.photos/seed/step24/400/300', 0, NULL, CURRENT_TIMESTAMP),
(25, 4, 'WASH', 1, '清洗整鸡', '整鸡里外冲洗干净，重点冲净腹腔血水与残留内脏膜，沥干水分。', 'https://picsum.photos/seed/step25/400/300', 0, NULL, CURRENT_TIMESTAMP),
(26, 4, 'CUT', 1, '切姜葱', '姜一半切厚片入锅用，一半切细蓉做蘸料；大葱葱段入锅，葱白部分切细蓉与姜蓉混合，加盐2克。', 'https://picsum.photos/seed/step26/400/300', 0, NULL, CURRENT_TIMESTAMP),
(27, 4, 'COOK', 1, '虾眼水浸煮', '大锅水烧开后放姜片葱段，提鸡头三起三落后整鸡入锅，转小火保持约90度微沸状态浸煮15分钟。', 'https://picsum.photos/seed/step27/400/300', 900, '小火', CURRENT_TIMESTAMP),
(28, 4, 'COOK', 2, '冰水过凉', '关火后将鸡迅速捞入冰水中浸10分钟，热胀冷缩让鸡皮爽脆、肉质收紧。', 'https://picsum.photos/seed/step28/400/300', 600, NULL, CURRENT_TIMESTAMP),
(29, 4, 'COOK', 3, '制姜葱油', '食用油15毫升小火加热至微微冒烟，浇在姜葱蓉上激出香味，加生抽10毫升拌匀成蘸料。', 'https://picsum.photos/seed/step29/400/300', 60, '小火', CURRENT_TIMESTAMP),
(30, 4, 'PLATE', 1, '斩件摆盘', '鸡沥干后斩成均匀块状，按原鸡形码盘，姜葱蘸料碟置于盘边。', 'https://picsum.photos/seed/step30/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱5：松鼠桂鱼（8 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(31, 5, 'PREPARE', 1, '备鱼调汁', '鳜鱼1条约600克备好；调糖醋汁：白糖40克、醋30毫升、西红柿50克切碎、清水60毫升、淀粉8克拌匀备用。', 'https://picsum.photos/seed/step31/400/300', 0, NULL, CURRENT_TIMESTAMP),
(32, 5, 'WASH', 1, '清洗鳜鱼', '鳜鱼去鳞、去鳃、去内脏，流水冲净腹腔黑膜与血水，加料酒10毫升、盐2克涂抹鱼身去腥。', 'https://picsum.photos/seed/step32/400/300', 0, NULL, CURRENT_TIMESTAMP),
(33, 5, 'CUT', 1, '去骨剞花刀', '从鳃后下刀沿脊骨片下两侧鱼肉（尾部相连），剔除胸刺，在鱼肉面斜剞菱形花刀，深至鱼皮但不切破。', 'https://picsum.photos/seed/step33/400/300', 0, NULL, CURRENT_TIMESTAMP),
(34, 5, 'CUT', 2, '拍粉抖粉', '鱼肉与鱼头均匀拍上干淀粉50克，提起鱼尾抖去余粉，使每条花刀缝隙都沾粉且不结块。', 'https://picsum.photos/seed/step34/400/300', 0, NULL, CURRENT_TIMESTAMP),
(35, 5, 'COOK', 1, '初炸定型', '食用油烧至七成热，手提鱼尾让花刀面朝下淋油定型后整体入锅，炸3分钟至花刀根根绽开。', 'https://picsum.photos/seed/step35/400/300', 180, '大火', CURRENT_TIMESTAMP),
(36, 5, 'COOK', 2, '复炸上色', '油温回升后复炸约60秒至通体金黄酥脆，捞出沥油，摆入长盘呈昂首翘尾状。', 'https://picsum.photos/seed/step36/400/300', 60, '大火', CURRENT_TIMESTAMP),
(37, 5, 'COOK', 3, '熬汁浇汁', '锅留底油下糖醋汁，中火熬约90秒至浓稠透亮，趁热从头至尾浇在鱼身上，发出滋啦声响。', 'https://picsum.photos/seed/step37/400/300', 90, '中火', CURRENT_TIMESTAMP),
(38, 5, 'PLATE', 1, '整形上桌', '微调鱼头鱼尾使其上翘成松鼠回望状，汁亮形挺，立即上桌。', 'https://picsum.photos/seed/step38/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱6：荔枝肉（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(39, 6, 'PREPARE', 1, '腌肉调汁', '猪里脊300克备好，加料酒、盐1克腌10分钟；调汁：白糖25克、醋20毫升、清水40毫升、淀粉5克拌匀。', 'https://picsum.photos/seed/step39/400/300', 600, NULL, CURRENT_TIMESTAMP),
(40, 6, 'WASH', 1, '清洗马蹄', '马蹄100克削皮后流水洗净泥沙，蒜冲洗备用。', 'https://picsum.photos/seed/step40/400/300', 0, NULL, CURRENT_TIMESTAMP),
(41, 6, 'CUT', 1, '剞十字花刀', '里脊切4厘米见方厚块，表面剞细密十字花刀（深约三分之二），再裹上干淀粉25克；马蹄对半切开。', 'https://picsum.photos/seed/step41/400/300', 0, NULL, CURRENT_TIMESTAMP),
(42, 6, 'COOK', 1, '炸制成形', '食用油烧至六成热，下肉块炸2分钟，花刀受热卷曲成荔枝状，捞出沥油。', 'https://picsum.photos/seed/step42/400/300', 120, '中火', CURRENT_TIMESTAMP),
(43, 6, 'COOK', 2, '复炸增脆', '油温升高后复炸约60秒至表面金红酥脆，捞出控油。', 'https://picsum.photos/seed/step43/400/300', 60, '大火', CURRENT_TIMESTAMP),
(44, 6, 'COOK', 3, '熬汁合炒', '锅留底油爆香蒜片，倒入调味汁大火熬浓，下马蹄与荔枝肉块快速翻匀，约90秒收汁亮芡。', 'https://picsum.photos/seed/step44/400/300', 90, '大火', CURRENT_TIMESTAMP),
(45, 6, 'PLATE', 1, '装盘出菜', '盛入白瓷盘，红亮的荔枝肉间点缀雪白马蹄，色泽如一盘新摘荔枝。', 'https://picsum.photos/seed/step45/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱7：西湖醋鱼（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(46, 7, 'PREPARE', 1, '备鱼调汁', '草鱼1条约700克备好；调汁：白糖20克、醋40毫升、生抽15毫升、清水100毫升；姜20克备用；淀粉10克加水调成水淀粉。', 'https://picsum.photos/seed/step46/400/300', 0, NULL, CURRENT_TIMESTAMP),
(47, 7, 'WASH', 1, '清洗草鱼', '草鱼去鳞去鳃去内脏，反复冲净腹腔黑膜与血水，加料酒10毫升涂抹去腥。', 'https://picsum.photos/seed/step47/400/300', 0, NULL, CURRENT_TIMESTAMP),
(48, 7, 'CUT', 1, '雌雄片改刀', '草鱼从尾部下刀一剖为二，带脊骨一侧在背肉厚处划一长刀（牡丹片），便于受热均匀；姜切细末。', 'https://picsum.photos/seed/step48/400/300', 0, NULL, CURRENT_TIMESTAMP),
(49, 7, 'COOK', 1, '氽煮断生', '宽水烧开后下鱼（皮朝上），再沸后转小火加盖氽3分钟，以筷子能轻松插入鱼背最厚处为准，捞出装盘。', 'https://picsum.photos/seed/step49/400/300', 180, '小火', CURRENT_TIMESTAMP),
(50, 7, 'COOK', 2, '熬制醋汁', '取氽鱼原汤150毫升入锅，加入调味汁与一半姜末，中火熬2分钟使酸甜融合。', 'https://picsum.photos/seed/step50/400/300', 120, '中火', CURRENT_TIMESTAMP),
(51, 7, 'COOK', 3, '勾芡收汁', '淋入水淀粉勾薄芡，小火推匀约60秒至汤汁呈琥珀色米汤芡。', 'https://picsum.photos/seed/step51/400/300', 60, '小火', CURRENT_TIMESTAMP),
(52, 7, 'PLATE', 1, '浇汁撒姜', '将醋汁均匀浇在鱼身上，撒剩余姜末，趁热上桌，吃的就是那一口似蟹的鲜。', 'https://picsum.photos/seed/step52/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱8：剁椒鱼头（8 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(53, 8, 'PREPARE', 1, '称重备料', '胖头鱼头1个约800克、剁椒100克备好；蒸鱼盘垫两根筷子架空鱼头，利于蒸汽循环。', 'https://picsum.photos/seed/step53/400/300', 0, NULL, CURRENT_TIMESTAMP),
(54, 8, 'PREPARE', 2, '腌制去腥', '鱼头均匀抹上盐2克与料酒15毫升，腌制10分钟去腥入底味。', 'https://picsum.photos/seed/step54/400/300', 600, NULL, CURRENT_TIMESTAMP),
(55, 8, 'WASH', 1, '清洗鱼头', '鱼头去鳃，流水反复冲净牙缝血水与腹腔黑膜，黑膜是腥味主要来源务必去净。', 'https://picsum.photos/seed/step55/400/300', 0, NULL, CURRENT_TIMESTAMP),
(56, 8, 'CUT', 1, '开边切配', '鱼头从下颌处对半劈开但背部相连，平摊成蝴蝶状，脊骨厚处划一刀；姜20克切丝、蒜15克切末、大葱切葱花。', 'https://picsum.photos/seed/step56/400/300', 0, NULL, CURRENT_TIMESTAMP),
(57, 8, 'COOK', 1, '大火蒸制', '鱼头铺姜丝、盖满剁椒与一半蒜末，水沸后入蒸锅，大火足汽蒸12分钟。', 'https://picsum.photos/seed/step57/400/300', 720, '大火', CURRENT_TIMESTAMP),
(58, 8, 'COOK', 2, '关火虚蒸', '时间到后不开盖，关火虚蒸3分钟，让余温把鱼脑部位焖透。', 'https://picsum.photos/seed/step58/400/300', 180, NULL, CURRENT_TIMESTAMP),
(59, 8, 'COOK', 3, '热油激香', '撒葱花与剩余蒜末，食用油30毫升烧至冒烟，趁热浇淋在剁椒上激出香气，约60秒完成。', 'https://picsum.photos/seed/step59/400/300', 60, '大火', CURRENT_TIMESTAMP),
(60, 8, 'PLATE', 1, '原盘上桌', '倒掉盘底多余腥水后原盘上桌，红椒白肉热气腾腾，配一份手工面拌汤汁最佳。', 'https://picsum.photos/seed/step60/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱9：黄山臭鳜鱼（7 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(61, 9, 'PREPARE', 1, '备鱼回温', '腌制好的臭鳜鱼1条约600克提前20分钟回至室温；五花肉50克、豆瓣酱15克、姜蒜备好。', 'https://picsum.photos/seed/step61/400/300', 0, NULL, CURRENT_TIMESTAMP),
(62, 9, 'WASH', 1, '冲洗表面', '臭鳜鱼轻轻冲洗表面盐渍与黏液（不可久泡以免流失风味），厨房纸吸干水分防煎时溅油。', 'https://picsum.photos/seed/step62/400/300', 0, NULL, CURRENT_TIMESTAMP),
(63, 9, 'CUT', 1, '改刀切配', '鱼身两面各剞3条一字刀便于入味；五花肉切0.5厘米小丁；姜15克切片、蒜10克拍松、干辣椒剪段。', 'https://picsum.photos/seed/step63/400/300', 0, NULL, CURRENT_TIMESTAMP),
(64, 9, 'COOK', 1, '煎鱼定香', '食用油30毫升润锅，下鱼中火两面各煎2分钟至金黄微焦，盛出备用。', 'https://picsum.photos/seed/step64/400/300', 240, '中火', CURRENT_TIMESTAMP),
(65, 9, 'COOK', 2, '小火烧制', '余油煸香五花肉丁出油，下豆瓣酱、姜蒜、干辣椒炒香，加生抽15毫升与热水没过鱼身一半，放回鱼小火烧10分钟，中途晃锅防粘。', 'https://picsum.photos/seed/step65/400/300', 600, '小火', CURRENT_TIMESTAMP),
(66, 9, 'COOK', 3, '大火收汁', '转大火收汁约2分钟，边收边将汤汁不断淋在鱼身，至汁浓亮油。', 'https://picsum.photos/seed/step66/400/300', 120, '大火', CURRENT_TIMESTAMP),
(67, 9, 'PLATE', 1, '装盘淋汁', '整鱼滑入长盘，浇上锅中浓汁与肉丁，鱼肉呈蒜瓣状即为火候到位。', 'https://picsum.photos/seed/step67/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- 菜谱10：低卡鸡胸时蔬沙拉（6 步）
INSERT INTO recipe_step (id, recipe_id, phase, step_index, action_title, detail, media_url, timer_sec, fire_power, created_at) VALUES
(68, 10, 'PREPARE', 1, '称重备料', '鸡胸肉150克、西兰花100克、生菜80克、西红柿80克、黄瓜80克、鸡蛋1个备好；调油醋汁：食用油5毫升、醋10毫升、盐1克摇匀。', 'https://picsum.photos/seed/step68/400/300', 0, NULL, CURRENT_TIMESTAMP),
(69, 10, 'WASH', 1, '洗净时蔬', '生菜逐叶掰开冷水浸洗后甩干；西兰花、西红柿、黄瓜流水冲净；西兰花可加少许盐浸泡5分钟去虫卵。', 'https://picsum.photos/seed/step69/400/300', 0, NULL, CURRENT_TIMESTAMP),
(70, 10, 'CUT', 1, '切配食材', '西兰花掰成一口大小的小朵；黄瓜切薄片；西红柿切月牙角；生菜手撕成大片避免刀切氧化。', 'https://picsum.photos/seed/step70/400/300', 0, NULL, CURRENT_TIMESTAMP),
(71, 10, 'COOK', 1, '水煮鸡胸与蛋', '冷水下鸡胸肉与鸡蛋，加料酒5毫升，水开后转中火煮8分钟关火，鸡胸在汤中浸5分钟后捞出，鸡蛋过凉剥壳。', 'https://picsum.photos/seed/step71/400/300', 480, '中火', CURRENT_TIMESTAMP),
(72, 10, 'COOK', 2, '焯烫西兰花', '另起锅水开加盐1克，下西兰花大火焯90秒保持翠绿脆嫩，捞出过凉沥干。', 'https://picsum.photos/seed/step72/400/300', 90, '大火', CURRENT_TIMESTAMP),
(73, 10, 'PLATE', 1, '拌制装盘', '鸡胸顺纹撕成细丝，鸡蛋对半切开，与所有时蔬在大碗中混合，淋入油醋汁轻拌均匀，装入浅口沙拉碗。', 'https://picsum.photos/seed/step73/400/300', 0, NULL, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------
-- 5. 菜谱食材关联 recipe_ingredient（77 条；名称与食材库一致）
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

-- ---------------------------------------------------------------
-- 6. 菜谱标签关联 recipe_tag_relation（37 条；1适宜 2慎用）
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
-- 松鼠桂鱼：适宜 儿童适宜/下饭菜；慎用 痛风慎用/控糖人群
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
(37, 10, 9,  1);

-- ---------------------------------------------------------------
-- 7. 智能食材替换规则 recipe_substitute_rule（7 条）
-- 场景：减脂/控糖/素食/低嘌呤
-- ---------------------------------------------------------------
INSERT INTO recipe_substitute_rule (id, source_ingredient_id, target_ingredient_id, scene, reason, created_at) VALUES
(1, 2,  1,  '减脂',   '同等饱腹感下每100g减少约435kcal', CURRENT_TIMESTAMP),
(2, 31, 32, '控糖',   '木糖醇升糖指数远低于蔗糖，几乎不引起血糖波动，适合控糖人群替代白糖', CURRENT_TIMESTAMP),
(3, 4,  6,  '素食',   '豆腐提供优质植物蛋白，口感软嫩，是肉末的经典素食替代', CURRENT_TIMESTAMP),
(4, 11, 1,  '低嘌呤', '鸡胸肉嘌呤含量明显低于虾仁，对痛风人群更友好且蛋白质更高', CURRENT_TIMESTAMP),
(5, 2,  6,  '素食',   '豆腐脂肪含量低且富含植物蛋白，可替代五花肉实现素食改造', CURRENT_TIMESTAMP),
(6, 3,  1,  '减脂',   '鸡胸肉脂肪更低、蛋白更高，每100g减少约22kcal', CURRENT_TIMESTAMP),
(7, 31, 32, '减脂',   '木糖醇热量约为蔗糖的60%，减脂期可有效减少精制糖摄入', CURRENT_TIMESTAMP);

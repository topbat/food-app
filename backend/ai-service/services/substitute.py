# -*- coding: utf-8 -*-
"""
智能食材替换规则引擎。

按健康场景（减脂 / 控糖 / 素食 / 低嘌呤）内置替换规则字典，
对入参食材逐个匹配并产出替换建议（字段统一小驼峰，符合接口契约）。

TODO: 此处可接入大模型API（如 Claude）提升泛化能力 ——
      当规则字典未命中时，可将「食材列表 + 场景」拼装为提示词调用大模型，
      生成更泛化的替换建议后再映射回统一返回结构。
"""

import logging

logger = logging.getLogger("ai-service.substitute")

# ============================================================
# 内置规则字典：场景 -> [ { source, target, reason, calorie_saved_per_100g } ]
# calorie_saved_per_100g：每 100g 替换后大约节省的热量（kcal），仅供参考
# ============================================================
SUBSTITUTE_RULES = {
    "减脂": [
        {
            "source": "五花肉",
            "target": "鸡胸肉",
            "reason": "五花肉脂肪含量高（约59g/100g），替换为高蛋白低脂的鸡胸肉，减脂期更友好",
            "calorie_saved_per_100g": 395,
        },
        {
            "source": "白糖",
            "target": "木糖醇",
            "reason": "木糖醇甜度接近白糖但热量更低、升糖指数低，减脂期可减少精制糖摄入",
            "calorie_saved_per_100g": 160,
        },
        {
            "source": "食用油",
            "target": "食用油（用量减半）",
            "reason": "建议将食用油用量减半并改用不粘锅烹饪，可显著降低整道菜的脂肪与热量",
            "calorie_saved_per_100g": 450,
        },
    ],
    "控糖": [
        {
            "source": "白糖",
            "target": "木糖醇",
            "reason": "木糖醇升糖指数（GI≈8）远低于白糖（GI≈84），控糖人群更适宜",
            "calorie_saved_per_100g": 160,
        },
        {
            "source": "土豆",
            "target": "西兰花",
            "reason": "土豆碳水含量高、升糖较快，替换为低碳水高纤维的西兰花有助平稳血糖",
            "calorie_saved_per_100g": 43,
        },
    ],
    "素食": [
        {
            "source": "五花肉",
            "target": "豆腐",
            "reason": "豆腐富含植物蛋白与钙质，可替代五花肉提供蛋白质，符合素食饮食",
            "calorie_saved_per_100g": 487,
        },
        {
            "source": "鸡胸肉",
            "target": "豆腐",
            "reason": "豆腐是优质植物蛋白来源，可替代鸡胸肉满足素食者蛋白质需求",
            "calorie_saved_per_100g": 52,
        },
        {
            "source": "虾仁",
            "target": "杏鲍菇",
            "reason": "杏鲍菇口感弹嫩接近虾仁，鲜味足且为纯植物食材，适合素食者",
            "calorie_saved_per_100g": 58,
        },
    ],
    "低嘌呤": [
        {
            "source": "虾仁",
            "target": "鸡胸肉",
            "reason": "虾类嘌呤含量较高，痛风/高尿酸人群建议替换为低嘌呤的鸡胸肉",
            "calorie_saved_per_100g": 0,
        },
        {
            "source": "鲈鱼",
            "target": "鸡蛋",
            "reason": "鱼类嘌呤偏高，鸡蛋几乎不含嘌呤且蛋白质优质，低嘌呤饮食首选",
            "calorie_saved_per_100g": 0,
        },
    ],
}


def get_substitute_suggestions(ingredients: list, scene: str) -> dict:
    """
    根据场景对入参食材列表逐个匹配替换规则，生成替换建议。

    参数:
        ingredients: 食材列表，形如 [{"name": "五花肉", "amount": 200}, ...]
        scene:       健康场景（减脂 / 控糖 / 素食 / 低嘌呤）

    返回:
        {"suggestions": [...], "tip": "..."}，suggestions 字段统一小驼峰：
        sourceName / targetName / reason / calorieSavedPer100g
    """
    rules = SUBSTITUTE_RULES.get(scene)
    if rules is None:
        # 未知场景：关键判断分支，记录日志并返回空建议
        logger.info("替换规则引擎：未知场景「%s」，无内置规则，返回空建议", scene)
        return {
            "suggestions": [],
            "tip": f"暂不支持场景「{scene}」，当前支持：{('、'.join(SUBSTITUTE_RULES.keys()))}",
        }

    suggestions = []
    for item in ingredients:
        name = (item.get("name") or "").strip()
        if not name:
            continue
        matched = False
        for rule in rules:
            # 双向模糊匹配：入参食材名包含规则源食材，或规则源食材包含入参名
            if rule["source"] in name or name in rule["source"]:
                matched = True
                logger.info(
                    "替换规则命中：场景=%s，食材「%s」→「%s」（每100g约省%skcal）",
                    scene, name, rule["target"], rule["calorie_saved_per_100g"],
                )
                suggestions.append({
                    "sourceName": name,
                    "targetName": rule["target"],
                    "reason": rule["reason"],
                    "calorieSavedPer100g": rule["calorie_saved_per_100g"],
                })
                break
        if not matched:
            logger.info("替换规则未命中：场景=%s，食材「%s」无内置替换规则", scene, name)

    if not suggestions:
        # 全部未命中：返回空列表并附带提示
        logger.info("替换规则引擎：场景=%s，所有食材均未命中规则，返回空建议", scene)
        return {
            "suggestions": [],
            "tip": "当前食材在该场景下暂无替换建议，原方案即可放心烹饪",
        }

    # TODO: 此处可接入大模型API（如 Claude）提升泛化能力 ——
    #       对未命中的食材调用大模型补充建议，并与规则结果合并去重。
    return {"suggestions": suggestions, "tip": ""}

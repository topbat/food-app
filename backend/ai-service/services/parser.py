# -*- coding: utf-8 -*-
"""
自由文本「五步法」菜谱解析器。

用启发式规则把一段自由文本菜谱拆解为产品需求文档第四节定义的菜谱 JSON Schema：
1. 菜名取第一句或首行；
2. 其余句子按关键词归类到五阶段（前期准备 / 洗 / 切 / 煮 / 装盘），无法归类默认前期准备；
3. 正则提取「数字+单位」（克/g/毫升/ml/分钟）填充 detail 与 timer_minutes；
4. 识别「大火/中火/小火」填充 fire_control；
5. nutrition 字段以 0 占位（需人工或模型补全），recipe_id 用 uuid 生成。

TODO: 此处可接入大模型API（如 Claude）提升泛化能力 ——
      可将自由文本与目标 JSON Schema 一并发给大模型做结构化抽取，
      启发式结果可作为兜底或用于校验大模型输出。
"""

import re
import uuid
import logging

logger = logging.getLogger("ai-service.parser")

# ============================================================
# 五阶段关键词表：按列表顺序优先匹配（洗 → 切 → 煮 → 装盘 → 前期准备）
# 句子中最先命中的阶段即为该句归属阶段；全部未命中默认「前期准备」
# ============================================================
PHASE_KEYWORDS = [
    ("洗", ["洗", "冲", "泡"]),
    ("切", ["切", "剁", "改刀", "丁", "丝", "片"]),
    ("煮", ["炒", "煮", "炖", "蒸", "炸", "焖", "烧", "火"]),
    ("装盘", ["盘", "摆", "撒", "点缀"]),
    ("前期准备", ["腌", "称", "备", "解冻"]),
]

# 各阶段的默认动作标题（action 字段）
PHASE_ACTION = {
    "前期准备": "备料与预处理",
    "洗": "清洗处理",
    "切": "改刀切配",
    "煮": "烹制",
    "装盘": "摆盘出餐",
}

# 阶段输出顺序（与五步法流程一致）
PHASE_ORDER = ["前期准备", "洗", "切", "煮", "装盘"]

# 数字+单位 提取（克/g/毫升/ml/分钟），如 “150g”“20毫升”“15分钟”
RE_QUANTITY = re.compile(r"(\d+(?:\.\d+)?)\s*(克|g|毫升|ml|分钟)", re.IGNORECASE)
# 计时提取：N分钟 -> timer_minutes
RE_TIMER = re.compile(r"(\d+(?:\.\d+)?)\s*分钟")
# 火候提取：大火/中火/小火
RE_FIRE = re.compile(r"(大火|中火|小火)")


def _split_sentences(text: str) -> list:
    """
    把自由文本按句子切分：以换行、中英文句号、分号、感叹号、问号为分隔符
    （英文句号后跟数字视为小数点，不切分），返回去除空白后的非空句子列表。
    """
    parts = re.split(r"[\n。；;！!？?]+|\.(?!\d)", text)
    return [p.strip() for p in parts if p and p.strip()]


def _classify_phase(sentence: str) -> str:
    """
    按关键词打分把单个句子归类到五阶段之一。

    统计句子中各阶段关键词的出现总次数，取得分最高的阶段；
    得分相同则按 洗 → 切 → 煮 → 装盘 → 前期准备 的优先顺序取先者；
    全部未命中（得分均为 0）默认返回「前期准备」。
    打分制可避免「下鸡丁翻炒」这类句子因先匹配到「丁」而被误归为「切」。
    """
    best_phase, best_score, best_kws = None, 0, []
    for phase, keywords in PHASE_KEYWORDS:
        hit_kws = [kw for kw in keywords if kw in sentence]
        score = sum(sentence.count(kw) for kw in hit_kws)
        if score > best_score:
            best_phase, best_score, best_kws = phase, score, hit_kws
    if best_phase is None:
        # 关键判断分支：未命中任何关键词，走默认归类
        logger.info("阶段识别未命中任何关键词，默认归类「前期准备」，句子：%s", sentence)
        return "前期准备"
    # 关键判断分支：记录阶段识别命中情况
    logger.info("阶段识别命中：关键词%s（得分%s）→ 阶段「%s」，句子：%s",
                "、".join(best_kws), best_score, best_phase, sentence)
    return best_phase


def _build_step(phase: str, sentence: str) -> dict:
    """
    把一个句子构造成步骤对象：填充 phase / action / detail / media_url，
    并按正则提取 timer_minutes（N分钟）与 fire_control（大火/中火/小火）。
    """
    step = {
        "phase": phase,
        "action": PHASE_ACTION[phase],
        "detail": sentence,
        "media_url": "",  # 多媒体地址占位，需后续人工补充
    }

    # 提取数字+单位（克/g/毫升/ml/分钟），仅用于日志与计时填充，detail 保留原句
    quantities = ["".join(m) for m in RE_QUANTITY.findall(sentence)]
    if quantities:
        logger.debug("数量提取：%s ← %s", quantities, sentence)

    # 提取计时（分钟）
    timer_match = RE_TIMER.search(sentence)
    if timer_match:
        step["timer_minutes"] = int(float(timer_match.group(1)))
        logger.info("计时识别：步骤含「%s分钟」，填充 timer_minutes=%s", timer_match.group(1), step["timer_minutes"])

    # 提取火候（大火/中火/小火）
    fire_match = RE_FIRE.search(sentence)
    if fire_match:
        step["fire_control"] = fire_match.group(1)
        logger.info("火候识别：步骤含「%s」，填充 fire_control", fire_match.group(1))

    return step


def parse_free_text(free_text: str) -> dict:
    """
    把自由文本菜谱启发式解析为产品需求文档第四节的菜谱 JSON Schema。

    流程：
    1. 切句：第一句/首行作为菜名（recipe_name），其余句子作为步骤来源；
    2. 逐句按关键词归类五阶段并构造步骤，按 前期准备→洗→切→煮→装盘 排序输出；
    3. recipe_id 用 uuid 生成；nutrition_per_serving 以 0 占位（需人工/模型补全）。

    参数:
        free_text: 自由文本菜谱内容（非空）

    返回:
        完整菜谱 JSON Schema（dict）
    """
    sentences = _split_sentences(free_text)
    logger.info("解析器开始：共切分出 %s 个句子", len(sentences))

    # 菜名取第一句或首行；只有一句时该句既作菜名也不再产生步骤
    recipe_name = sentences[0][:30] if sentences else "未命名菜谱"
    body_sentences = sentences[1:]
    logger.info("菜名识别：取第一句「%s」", recipe_name)

    # 逐句归类五阶段
    phase_steps = {phase: [] for phase in PHASE_ORDER}
    for sentence in body_sentences:
        phase = _classify_phase(sentence)
        phase_steps[phase].append(_build_step(phase, sentence))

    # 按五步法阶段顺序平铺步骤
    steps = []
    for phase in PHASE_ORDER:
        steps.extend(phase_steps[phase])

    # TODO: 此处可接入大模型API（如 Claude）提升泛化能力 ——
    #       将 free_text 发给大模型直接产出结构化菜谱（含营养估算、人群标签），
    #       并与启发式结果做字段级合并。
    result = {
        "recipe_id": str(uuid.uuid4()),
        "recipe_name": recipe_name,
        "region": "",          # 菜系无法从文本可靠推断，留空待人工补全
        "tags": [],
        "nutrition_per_serving": {
            # 营养数据以 0 占位：启发式无法估算，需人工录入或接入大模型补全
            "calories_kcal": 0,
            "carbs_g": 0,
            "protein_g": 0,
            "fat_g": 0,
        },
        "suitable_crowd": [],
        "unsuitable_crowd": [],
        "steps": steps,
    }
    logger.info("解析器完成：菜名「%s」，共生成 %s 个步骤", recipe_name, len(steps))
    return result

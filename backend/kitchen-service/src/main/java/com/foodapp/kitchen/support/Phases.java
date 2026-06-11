package com.foodapp.kitchen.support;

import java.util.List;
import java.util.Map;

/**
 * 五步法阶段常量：阶段顺序与进度权重。
 * 阶段顺序 PREPARE→WASH→CUT→COOK→PLATE，权重 10/5/15/60/10（与菜谱服务契约 phaseWeights 一致）。
 */
public final class Phases {

    /** 阶段流转顺序（不可逆） */
    public static final List<String> ORDER = List.of("PREPARE", "WASH", "CUT", "COOK", "PLATE");

    /** 阶段进度权重（总和=100） */
    public static final Map<String, Integer> WEIGHTS = Map.of(
            "PREPARE", 10,
            "WASH", 5,
            "CUT", 15,
            "COOK", 60,
            "PLATE", 10
    );

    /** 阶段中文名（用于日志与语音反馈文案） */
    public static final Map<String, String> NAMES = Map.of(
            "PREPARE", "前期准备",
            "WASH", "洗",
            "CUT", "切",
            "COOK", "煮",
            "PLATE", "装盘"
    );

    private Phases() {
    }

    /**
     * 获取阶段在流转顺序中的下标。
     *
     * @param phase 阶段编码
     * @return 下标；未知阶段返回 -1
     */
    public static int indexOf(String phase) {
        return ORDER.indexOf(phase);
    }
}

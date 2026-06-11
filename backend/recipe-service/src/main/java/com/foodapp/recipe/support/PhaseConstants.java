package com.foodapp.recipe.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 五步法阶段常量：阶段顺序与进度权重（准备10%-洗5%-切15%-煮60%-装盘10%）。
 */
public final class PhaseConstants {

    /** 阶段固定顺序 */
    public static final List<String> PHASE_ORDER = List.of("PREPARE", "WASH", "CUT", "COOK", "PLATE");

    /** 阶段中文名 */
    public static final Map<String, String> PHASE_NAMES = Map.of(
            "PREPARE", "前期准备", "WASH", "洗", "CUT", "切", "COOK", "煮", "PLATE", "装盘");

    private PhaseConstants() {
    }

    /**
     * 获取阶段进度权重（固定顺序的 LinkedHashMap，便于前端按序渲染进度条）。
     *
     * @return 阶段→权重 映射
     */
    public static Map<String, Integer> phaseWeights() {
        Map<String, Integer> weights = new LinkedHashMap<>();
        weights.put("PREPARE", 10);
        weights.put("WASH", 5);
        weights.put("CUT", 15);
        weights.put("COOK", 60);
        weights.put("PLATE", 10);
        return weights;
    }

    /**
     * 获取阶段在五步法中的序号（用于步骤排序）。
     *
     * @param phase 阶段编码
     * @return 序号（未知阶段排最后）
     */
    public static int orderOf(String phase) {
        int idx = PHASE_ORDER.indexOf(phase);
        return idx < 0 ? Integer.MAX_VALUE : idx;
    }
}

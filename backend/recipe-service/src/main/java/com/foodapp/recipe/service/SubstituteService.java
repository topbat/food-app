package com.foodapp.recipe.service;

import com.foodapp.recipe.entity.RecipeIngredient;
import com.foodapp.recipe.entity.RecipeIngredientLib;
import com.foodapp.recipe.entity.RecipeInfo;
import com.foodapp.recipe.entity.RecipeSubstituteRule;
import com.foodapp.recipe.repository.RecipeIngredientLibRepository;
import com.foodapp.recipe.repository.RecipeIngredientRepository;
import com.foodapp.recipe.repository.RecipeSubstituteRuleRepository;
import com.foodapp.recipe.vo.SubstituteResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 智能食材替换服务。
 * 按场景（减脂/控糖/素食/低嘌呤）匹配替换规则，并基于食材库营养数据动态计算替换前后的热量差。
 */
@Service
public class SubstituteService {

    private static final Logger log = LoggerFactory.getLogger(SubstituteService.class);

    /** 按克/毫升计量的单位集合（只有这类单位可直接换算热量） */
    private static final Set<String> GRAM_UNITS = Set.of("克", "毫升", "g", "ml");

    private final RecipeQueryService recipeQueryService;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeIngredientLibRepository ingredientLibRepository;
    private final RecipeSubstituteRuleRepository substituteRuleRepository;

    public SubstituteService(RecipeQueryService recipeQueryService,
                             RecipeIngredientRepository recipeIngredientRepository,
                             RecipeIngredientLibRepository ingredientLibRepository,
                             RecipeSubstituteRuleRepository substituteRuleRepository) {
        this.recipeQueryService = recipeQueryService;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.ingredientLibRepository = ingredientLibRepository;
        this.substituteRuleRepository = substituteRuleRepository;
    }

    /**
     * 计算指定菜谱在指定场景下的智能替换建议与热量变化。
     *
     * @param recipeId 菜谱ID
     * @param scene    适用场景（减脂/控糖/素食/低嘌呤）
     * @return 替换结果（替换前后热量、明细列表）
     */
    public SubstituteResultVO substitute(Long recipeId, String scene) {
        RecipeInfo info = recipeQueryService.requireOnSale(recipeId);
        List<RecipeIngredient> ingredients = recipeIngredientRepository.findByRecipeId(recipeId);

        List<Long> ingredientIds = ingredients.stream().map(RecipeIngredient::getIngredientId).distinct().toList();
        List<RecipeSubstituteRule> rules = ingredientIds.isEmpty()
                ? List.of()
                : substituteRuleRepository.findBySourceIngredientIdInAndScene(ingredientIds, scene.trim());

        SubstituteResultVO result = new SubstituteResultVO();
        result.setOriginalCalories(info.getCaloriesKcal());

        // 关键判断：该场景下无任何可替换规则，返回空明细（前端展示"暂无替换建议"）
        if (rules.isEmpty()) {
            log.info("[智能替换] 菜谱{}({}) 场景[{}] 无可替换食材", recipeId, info.getTitle(), scene);
            result.setNewCalories(info.getCaloriesKcal());
            result.setCalorieDiff(BigDecimal.ZERO);
            result.setSubstitutions(List.of());
            return result;
        }

        // 批量加载涉及的食材营养数据（原食材 + 目标食材），避免循环查库
        Set<Long> libIds = rules.stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getSourceIngredientId(), r.getTargetIngredientId()))
                .collect(Collectors.toSet());
        Map<Long, RecipeIngredientLib> libMap = ingredientLibRepository.findAllById(libIds).stream()
                .collect(Collectors.toMap(RecipeIngredientLib::getId, l -> l));
        Map<Long, RecipeIngredient> usageMap = ingredients.stream()
                .collect(Collectors.toMap(RecipeIngredient::getIngredientId, i -> i, (a, b) -> a));

        List<SubstituteResultVO.SubstitutionItemVO> items = new ArrayList<>();
        BigDecimal totalDiff = BigDecimal.ZERO;
        int servings = info.getServings() == null || info.getServings() < 1 ? 1 : info.getServings();

        for (RecipeSubstituteRule rule : rules) {
            RecipeIngredientLib source = libMap.get(rule.getSourceIngredientId());
            RecipeIngredientLib target = libMap.get(rule.getTargetIngredientId());
            RecipeIngredient usage = usageMap.get(rule.getSourceIngredientId());
            // 关键判断：规则引用的食材在库中缺失则跳过该条（数据完整性兜底）
            if (source == null || target == null || usage == null) {
                log.warn("[智能替换] 规则{}引用的食材数据缺失, 跳过", rule.getId());
                continue;
            }

            BigDecimal itemDiff = BigDecimal.ZERO;
            // 关键判断：仅克/毫升计量的食材可精确换算热量差，其他单位只给替换建议不算热量
            if (usage.getUnit() != null && GRAM_UNITS.contains(usage.getUnit())) {
                BigDecimal per100Diff = source.getCaloriesPer100g().subtract(target.getCaloriesPer100g());
                itemDiff = per100Diff.multiply(usage.getAmount())
                        .divide(BigDecimal.valueOf(100L * servings), 1, RoundingMode.HALF_UP);
            } else {
                log.info("[智能替换] 食材[{}]单位为[{}], 无法精确换算热量, 仅给出替换建议",
                        source.getName(), usage.getUnit());
            }

            SubstituteResultVO.SubstitutionItemVO item = new SubstituteResultVO.SubstitutionItemVO();
            item.setSourceName(source.getName());
            item.setTargetName(target.getName());
            item.setReason(rule.getReason());
            item.setCalorieDiff(itemDiff);
            items.add(item);
            totalDiff = totalDiff.add(itemDiff);
            log.info("[智能替换] 替换命中: {} -> {}, 减少{}kcal/份 (场景: {})",
                    source.getName(), target.getName(), itemDiff, scene);
        }

        BigDecimal newCalories = info.getCaloriesKcal().subtract(totalDiff).max(BigDecimal.ZERO)
                .setScale(1, RoundingMode.HALF_UP);
        result.setNewCalories(newCalories);
        result.setCalorieDiff(totalDiff.setScale(1, RoundingMode.HALF_UP));
        result.setSubstitutions(items);
        log.info("[智能替换] 菜谱{}({}) 场景[{}] 共{}项替换, 单份热量 {} -> {} kcal",
                recipeId, info.getTitle(), scene, items.size(), info.getCaloriesKcal(), newCalories);
        return result;
    }
}

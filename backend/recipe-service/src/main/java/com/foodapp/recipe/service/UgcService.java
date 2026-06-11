package com.foodapp.recipe.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.recipe.dto.UgcRecipeRequest;
import com.foodapp.recipe.entity.RecipeIngredient;
import com.foodapp.recipe.entity.RecipeIngredientLib;
import com.foodapp.recipe.entity.RecipeInfo;
import com.foodapp.recipe.entity.RecipeStep;
import com.foodapp.recipe.entity.RecipeTagRelation;
import com.foodapp.recipe.repository.RecipeIngredientLibRepository;
import com.foodapp.recipe.repository.RecipeIngredientRepository;
import com.foodapp.recipe.repository.RecipeInfoRepository;
import com.foodapp.recipe.repository.RecipeStepRepository;
import com.foodapp.recipe.repository.RecipeTagRelationRepository;
import com.foodapp.recipe.support.PhaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UGC 菜谱上传服务。
 * 强制五步法模板校验（PREPARE/WASH/CUT/COOK/PLATE 缺一不可），
 * 并基于食材库营养数据自动计算菜谱总热量与三大营养素。
 */
@Service
public class UgcService {

    private static final Logger log = LoggerFactory.getLogger(UgcService.class);

    /** 按克/毫升计量的单位集合（参与营养计算） */
    private static final Set<String> GRAM_UNITS = Set.of("克", "毫升", "g", "ml");

    private final RecipeInfoRepository recipeInfoRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeIngredientLibRepository ingredientLibRepository;
    private final RecipeTagRelationRepository tagRelationRepository;

    public UgcService(RecipeInfoRepository recipeInfoRepository,
                      RecipeStepRepository recipeStepRepository,
                      RecipeIngredientRepository recipeIngredientRepository,
                      RecipeIngredientLibRepository ingredientLibRepository,
                      RecipeTagRelationRepository tagRelationRepository) {
        this.recipeInfoRepository = recipeInfoRepository;
        this.recipeStepRepository = recipeStepRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.ingredientLibRepository = ingredientLibRepository;
        this.tagRelationRepository = tagRelationRepository;
    }

    /**
     * 用户上传 UGC 菜谱（五步法标准化模板）。
     * 入库状态为 2-待审核，人工审核通过后才对外可见（安全要求：UGC 内容先审后发）。
     *
     * @param userId  当前登录用户ID（作者）
     * @param request UGC 上传请求
     * @return 新建菜谱ID
     */
    @Transactional
    public Long createUgcRecipe(Long userId, UgcRecipeRequest request) {
        // 关键判断：五步法完整性校验——必须覆盖全部 5 个阶段
        Set<String> phases = request.getSteps().stream()
                .map(s -> s.getPhase() == null ? "" : s.getPhase().trim().toUpperCase())
                .collect(Collectors.toSet());
        for (String required : PhaseConstants.PHASE_ORDER) {
            if (!phases.contains(required)) {
                String phaseName = PhaseConstants.PHASE_NAMES.get(required);
                log.warn("[UGC上传] 用户{}上传菜谱[{}]五步法不完整, 缺少{}({})阶段",
                        userId, request.getTitle(), phaseName, required);
                throw new BusinessException(ResultCode.PARAM_ERROR, "五步法不完整，缺少「" + phaseName + "」阶段");
            }
        }

        // 校验食材均存在于食材库
        List<Long> ingredientIds = request.getIngredients().stream()
                .map(UgcRecipeRequest.IngredientItem::getIngredientId).distinct().toList();
        Map<Long, RecipeIngredientLib> libMap = ingredientLibRepository.findAllById(ingredientIds).stream()
                .collect(Collectors.toMap(RecipeIngredientLib::getId, l -> l));
        for (Long id : ingredientIds) {
            if (!libMap.containsKey(id)) {
                log.warn("[UGC上传] 用户{}引用了不存在的食材ID={}", userId, id);
                throw new BusinessException(ResultCode.NOT_FOUND, "食材不存在: ID=" + id);
            }
        }

        // 按食材用量自动计算整谱营养，再折算单人份
        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        for (UgcRecipeRequest.IngredientItem item : request.getIngredients()) {
            // 关键判断：仅克/毫升单位参与营养计算，"个/勺"等单位跳过并记录日志
            if (item.getUnit() == null || !GRAM_UNITS.contains(item.getUnit())) {
                log.info("[UGC上传] 食材ID={}单位[{}]不参与营养自动计算", item.getIngredientId(), item.getUnit());
                continue;
            }
            RecipeIngredientLib lib = libMap.get(item.getIngredientId());
            BigDecimal factor = item.getAmount().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            calories = calories.add(lib.getCaloriesPer100g().multiply(factor));
            carbs = carbs.add(lib.getCarbsPer100g().multiply(factor));
            protein = protein.add(lib.getProteinPer100g().multiply(factor));
            fat = fat.add(lib.getFatPer100g().multiply(factor));
        }
        BigDecimal servings = BigDecimal.valueOf(request.getServings());

        RecipeInfo info = new RecipeInfo();
        info.setTitle(request.getTitle());
        info.setCoverUrl(request.getCoverUrl());
        info.setCuisineType(request.getCuisineType());
        info.setDifficulty(request.getDifficulty());
        info.setTotalTimeMin(request.getTotalTimeMin());
        info.setServings(request.getServings());
        info.setCaloriesKcal(calories.divide(servings, 2, RoundingMode.HALF_UP));
        info.setCarbsG(carbs.divide(servings, 2, RoundingMode.HALF_UP));
        info.setProteinG(protein.divide(servings, 2, RoundingMode.HALF_UP));
        info.setFatG(fat.divide(servings, 2, RoundingMode.HALF_UP));
        info.setDescription(request.getDescription());
        info.setTips(request.getTips());
        info.setStatus(2);
        info.setSourceType(2);
        info.setAuthorId(userId);
        info.setViewCount(0);
        info.setLikeCount(0);
        recipeInfoRepository.save(info);

        // 保存五步法步骤
        for (UgcRecipeRequest.StepItem stepItem : request.getSteps()) {
            RecipeStep step = new RecipeStep();
            step.setRecipeId(info.getId());
            step.setPhase(stepItem.getPhase().trim().toUpperCase());
            step.setStepIndex(stepItem.getStepIndex());
            step.setActionTitle(stepItem.getActionTitle());
            step.setDetail(stepItem.getDetail());
            step.setMediaUrl(stepItem.getMediaUrl());
            step.setTimerSec(stepItem.getTimerSec() == null ? 0 : stepItem.getTimerSec());
            step.setFirePower(stepItem.getFirePower());
            recipeStepRepository.save(step);
        }

        // 保存食材关联（冗余食材名便于展示与"冰箱清理"检索）
        for (UgcRecipeRequest.IngredientItem item : request.getIngredients()) {
            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setRecipeId(info.getId());
            ingredient.setIngredientId(item.getIngredientId());
            ingredient.setIngredientName(libMap.get(item.getIngredientId()).getName());
            ingredient.setAmount(item.getAmount());
            ingredient.setUnit(item.getUnit());
            ingredient.setIsEssential(item.getIsEssential() == null ? 1 : item.getIsEssential());
            recipeIngredientRepository.save(ingredient);
        }

        // 保存适宜/慎用标签关联（去重）
        saveTagRelations(info.getId(), request.getSuitableTagIds(), 1);
        saveTagRelations(info.getId(), request.getUnsuitableTagIds(), 2);

        log.info("[UGC上传] 用户{}上传菜谱[{}]成功, id={}, 单份热量={}kcal, 状态=待审核",
                userId, info.getTitle(), info.getId(), info.getCaloriesKcal());
        return info.getId();
    }

    /**
     * 保存菜谱标签关联。
     *
     * @param recipeId     菜谱ID
     * @param tagIds       标签ID列表（可空）
     * @param relationType 关系类型（1适宜 2慎用）
     */
    private void saveTagRelations(Long recipeId, List<Long> tagIds, int relationType) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : new HashSet<>(tagIds)) {
            RecipeTagRelation relation = new RecipeTagRelation();
            relation.setRecipeId(recipeId);
            relation.setTagId(tagId);
            relation.setRelationType(relationType);
            tagRelationRepository.save(relation);
        }
    }
}

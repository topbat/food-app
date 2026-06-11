package com.foodapp.recipe.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.recipe.entity.RecipeIngredient;
import com.foodapp.recipe.entity.RecipeInfo;
import com.foodapp.recipe.entity.RecipeStep;
import com.foodapp.recipe.entity.RecipeTag;
import com.foodapp.recipe.entity.RecipeTagRelation;
import com.foodapp.recipe.repository.RecipeInfoRepository;
import com.foodapp.recipe.repository.RecipeIngredientRepository;
import com.foodapp.recipe.repository.RecipeStepRepository;
import com.foodapp.recipe.repository.RecipeTagRelationRepository;
import com.foodapp.recipe.repository.RecipeTagRepository;
import com.foodapp.recipe.support.PhaseConstants;
import com.foodapp.recipe.vo.IngredientVO;
import com.foodapp.recipe.vo.PageResultVO;
import com.foodapp.recipe.vo.RecipeDetailVO;
import com.foodapp.recipe.vo.RecipeSummaryVO;
import com.foodapp.recipe.vo.StepVO;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 菜谱查询服务：分页动态查询、详情聚合、步骤平铺。
 */
@Service
public class RecipeQueryService {

    private static final Logger log = LoggerFactory.getLogger(RecipeQueryService.class);

    /** 菜系编码→中文名映射 */
    private static final Map<Integer, String> CUISINE_NAMES = Map.of(
            1, "川菜", 2, "鲁菜", 3, "粤菜", 4, "苏菜", 5, "闽菜",
            6, "浙菜", 7, "湘菜", 8, "徽菜", 9, "家常菜");

    private final RecipeInfoRepository recipeInfoRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final RecipeTagRelationRepository recipeTagRelationRepository;

    public RecipeQueryService(RecipeInfoRepository recipeInfoRepository,
                              RecipeStepRepository recipeStepRepository,
                              RecipeIngredientRepository recipeIngredientRepository,
                              RecipeTagRepository recipeTagRepository,
                              RecipeTagRelationRepository recipeTagRelationRepository) {
        this.recipeInfoRepository = recipeInfoRepository;
        this.recipeStepRepository = recipeStepRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeTagRepository = recipeTagRepository;
        this.recipeTagRelationRepository = recipeTagRelationRepository;
    }

    /**
     * 分页动态查询菜谱列表（只返回上架菜谱，按浏览量降序）。
     * 全部筛选条件可自由组合：菜系/难度/标题关键词/最大热量/标签名/食材清单（冰箱清理模式）。
     *
     * @param page        页码（从1开始）
     * @param size        每页条数
     * @param cuisineType 菜系编码（可空）
     * @param difficulty  难度（可空）
     * @param keyword     标题模糊关键词（可空）
     * @param maxCalories 最大单份热量（可空）
     * @param tagName     标签名（可空，仅匹配"适宜"关系）
     * @param ingredients 逗号分隔食材名（可空，匹配含任一食材的菜谱）
     * @return 分页结果
     */
    public PageResultVO<RecipeSummaryVO> pageQuery(int page, int size, Integer cuisineType, Integer difficulty,
                                                   String keyword, BigDecimal maxCalories, String tagName,
                                                   String ingredients) {
        // 关键判断：标签名筛选先解析为标签ID，标签不存在则直接返回空页（无须查询主表）
        Long tagId = null;
        if (tagName != null && !tagName.isBlank()) {
            Optional<RecipeTag> tag = recipeTagRepository.findByTagName(tagName.trim());
            if (tag.isEmpty()) {
                log.info("[菜谱查询] 标签[{}]不存在, 返回空结果", tagName);
                return new PageResultVO<>(0, page, size, List.of());
            }
            tagId = tag.get().getId();
        }
        final Long finalTagId = tagId;
        final List<String> ingredientNames = (ingredients == null || ingredients.isBlank())
                ? List.of()
                : Arrays.stream(ingredients.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();

        Specification<RecipeInfo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 安全与业务规则：仅返回上架菜谱
            predicates.add(cb.equal(root.get("status"), 1));
            if (cuisineType != null) {
                predicates.add(cb.equal(root.get("cuisineType"), cuisineType));
            }
            if (difficulty != null) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(root.get("title"), "%" + keyword.trim() + "%"));
            }
            if (maxCalories != null) {
                predicates.add(cb.le(root.get("caloriesKcal"), maxCalories));
            }
            if (finalTagId != null) {
                // 子查询：菜谱ID ∈ 拥有该"适宜"标签的菜谱集合
                Subquery<Long> tagSub = query.subquery(Long.class);
                var rel = tagSub.from(RecipeTagRelation.class);
                tagSub.select(rel.get("recipeId"))
                        .where(cb.equal(rel.get("tagId"), finalTagId), cb.equal(rel.get("relationType"), 1));
                predicates.add(root.get("id").in(tagSub));
            }
            if (!ingredientNames.isEmpty()) {
                // 子查询：冰箱清理模式——含任一指定食材的菜谱
                Subquery<Long> ingSub = query.subquery(Long.class);
                var ing = ingSub.from(RecipeIngredient.class);
                ingSub.select(ing.get("recipeId")).where(ing.get("ingredientName").in(ingredientNames));
                predicates.add(root.get("id").in(ingSub));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<RecipeInfo> result = recipeInfoRepository.findAll(spec,
                PageRequest.of(Math.max(page, 1) - 1, Math.min(Math.max(size, 1), 50),
                        Sort.by(Sort.Direction.DESC, "viewCount")));
        log.info("[菜谱查询] 条件: 菜系={}, 难度={}, 关键词={}, 最大热量={}, 标签={}, 食材={}, 命中{}条",
                cuisineType, difficulty, keyword, maxCalories, tagName, ingredientNames, result.getTotalElements());

        List<RecipeSummaryVO> list = assembleSummaries(result.getContent());
        return new PageResultVO<>(result.getTotalElements(), page, size, list);
    }

    /**
     * 查询菜谱详情聚合：基本信息 + 五步法分组步骤 + 食材清单 + 适宜/慎用标签 + 阶段权重。
     *
     * @param id 菜谱ID
     * @return 详情聚合 VO
     */
    public RecipeDetailVO detail(Long id) {
        RecipeInfo info = requireOnSale(id);

        RecipeDetailVO vo = new RecipeDetailVO();
        RecipeSummaryVO summary = assembleSummaries(List.of(info)).get(0);
        summary.setTips(info.getTips());
        summary.setServings(info.getServings());
        summary.setSourceType(info.getSourceType());
        vo.setInfo(summary);

        // 五步法分组：键固定 PREPARE→WASH→CUT→COOK→PLATE 顺序，组内按 stepIndex 升序
        List<RecipeStep> steps = recipeStepRepository.findByRecipeIdOrderByStepIndexAsc(id);
        Map<String, List<StepVO>> grouped = new LinkedHashMap<>();
        for (String phase : PhaseConstants.PHASE_ORDER) {
            grouped.put(phase, new ArrayList<>());
        }
        steps.stream()
                .sorted(Comparator.comparing((RecipeStep s) -> PhaseConstants.orderOf(s.getPhase()))
                        .thenComparing(RecipeStep::getStepIndex))
                .forEach(s -> grouped.computeIfAbsent(s.getPhase(), k -> new ArrayList<>()).add(toStepVO(s)));
        vo.setStepsByPhase(grouped);

        vo.setIngredients(recipeIngredientRepository.findByRecipeId(id).stream().map(this::toIngredientVO).toList());

        // 标签按关系类型拆分为 适宜 / 慎用
        List<RecipeTagRelation> relations = recipeTagRelationRepository.findByRecipeId(id);
        Map<Long, String> tagNames = loadTagNames(relations);
        vo.setSuitableTags(relations.stream().filter(r -> r.getRelationType() == 1)
                .map(r -> tagNames.get(r.getTagId())).filter(java.util.Objects::nonNull).toList());
        vo.setUnsuitableTags(relations.stream().filter(r -> r.getRelationType() == 2)
                .map(r -> tagNames.get(r.getTagId())).filter(java.util.Objects::nonNull).toList());

        vo.setPhaseWeights(PhaseConstants.phaseWeights());
        log.info("[菜谱详情] id={}, 标题={}, 步骤{}个, 食材{}种", id, info.getTitle(), steps.size(), vo.getIngredients().size());
        return vo;
    }

    /**
     * 查询菜谱步骤平铺列表（按五步法阶段顺序 + 阶段内序号排序，供厨房引擎服务调用）。
     *
     * @param id 菜谱ID
     * @return 步骤列表
     */
    public List<StepVO> flatSteps(Long id) {
        requireOnSale(id);
        return recipeStepRepository.findByRecipeIdOrderByStepIndexAsc(id).stream()
                .sorted(Comparator.comparing((RecipeStep s) -> PhaseConstants.orderOf(s.getPhase()))
                        .thenComparing(RecipeStep::getStepIndex))
                .map(this::toStepVO)
                .toList();
    }

    /**
     * 浏览数原子 +1。
     *
     * @param id 菜谱ID
     */
    @Transactional
    public void increaseView(Long id) {
        int updated = recipeInfoRepository.incrementViewCount(id);
        // 关键判断：更新行数为0说明菜谱不存在
        if (updated == 0) {
            log.warn("[菜谱浏览] 菜谱{}不存在, 浏览计数忽略", id);
        }
    }

    /**
     * 加载菜谱并校验上架状态（不存在或已下架抛 40400）。
     *
     * @param id 菜谱ID
     * @return 菜谱实体
     */
    public RecipeInfo requireOnSale(Long id) {
        RecipeInfo info = recipeInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "菜谱不存在"));
        // 关键判断：下架/待审核菜谱对外不可见
        if (info.getStatus() == null || info.getStatus() != 1) {
            log.warn("[菜谱查询] 菜谱{}状态={}, 对外不可见", id, info.getStatus());
            throw new BusinessException(ResultCode.NOT_FOUND, "菜谱不存在或已下架");
        }
        return info;
    }

    /**
     * 批量组装菜谱摘要（一次性批量查询标签，避免 N+1，性能要求）。
     *
     * @param recipes 菜谱实体列表
     * @return 摘要 VO 列表
     */
    private List<RecipeSummaryVO> assembleSummaries(List<RecipeInfo> recipes) {
        if (recipes.isEmpty()) {
            return List.of();
        }
        List<Long> ids = recipes.stream().map(RecipeInfo::getId).toList();
        List<RecipeTagRelation> relations = recipeTagRelationRepository.findByRecipeIdIn(ids).stream()
                .filter(r -> r.getRelationType() == 1).toList();
        Map<Long, String> tagNames = loadTagNames(relations);
        Map<Long, List<String>> recipeTags = relations.stream().collect(Collectors.groupingBy(
                RecipeTagRelation::getRecipeId,
                Collectors.mapping(r -> tagNames.get(r.getTagId()), Collectors.toList())));

        return recipes.stream().map(r -> {
            RecipeSummaryVO vo = new RecipeSummaryVO();
            vo.setId(r.getId());
            vo.setTitle(r.getTitle());
            vo.setCoverUrl(r.getCoverUrl());
            vo.setCuisineType(r.getCuisineType());
            vo.setCuisineName(CUISINE_NAMES.getOrDefault(r.getCuisineType(), "其他"));
            vo.setDifficulty(r.getDifficulty());
            vo.setTotalTimeMin(r.getTotalTimeMin());
            vo.setCaloriesKcal(r.getCaloriesKcal());
            vo.setCarbsG(r.getCarbsG());
            vo.setProteinG(r.getProteinG());
            vo.setFatG(r.getFatG());
            vo.setDescription(r.getDescription());
            vo.setViewCount(r.getViewCount());
            vo.setLikeCount(r.getLikeCount());
            vo.setTags(recipeTags.getOrDefault(r.getId(), List.of()).stream()
                    .filter(java.util.Objects::nonNull).toList());
            return vo;
        }).toList();
    }

    /**
     * 批量加载标签字典（标签ID→名称）。
     *
     * @param relations 标签关联列表
     * @return 标签ID→名称映射
     */
    private Map<Long, String> loadTagNames(List<RecipeTagRelation> relations) {
        List<Long> tagIds = relations.stream().map(RecipeTagRelation::getTagId).distinct().toList();
        if (tagIds.isEmpty()) {
            return Map.of();
        }
        return recipeTagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(RecipeTag::getId, RecipeTag::getTagName, (a, b) -> a));
    }

    /**
     * 步骤实体转 VO。
     */
    private StepVO toStepVO(RecipeStep s) {
        StepVO vo = new StepVO();
        vo.setId(s.getId());
        vo.setRecipeId(s.getRecipeId());
        vo.setPhase(s.getPhase());
        vo.setStepIndex(s.getStepIndex());
        vo.setActionTitle(s.getActionTitle());
        vo.setDetail(s.getDetail());
        vo.setMediaUrl(s.getMediaUrl());
        vo.setTimerSec(s.getTimerSec());
        vo.setFirePower(s.getFirePower());
        return vo;
    }

    /**
     * 食材关联实体转 VO。
     */
    private IngredientVO toIngredientVO(RecipeIngredient i) {
        IngredientVO vo = new IngredientVO();
        vo.setId(i.getId());
        vo.setIngredientId(i.getIngredientId());
        vo.setIngredientName(i.getIngredientName());
        vo.setAmount(i.getAmount());
        vo.setUnit(i.getUnit());
        vo.setIsEssential(i.getIsEssential());
        return vo;
    }
}

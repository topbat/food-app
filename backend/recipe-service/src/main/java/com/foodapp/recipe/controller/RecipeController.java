package com.foodapp.recipe.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.recipe.dto.SubstituteRequest;
import com.foodapp.recipe.dto.UgcRecipeRequest;
import com.foodapp.recipe.service.RecipeQueryService;
import com.foodapp.recipe.service.SubstituteService;
import com.foodapp.recipe.service.UgcService;
import com.foodapp.recipe.vo.PageResultVO;
import com.foodapp.recipe.vo.RecipeDetailVO;
import com.foodapp.recipe.vo.RecipeSummaryVO;
import com.foodapp.recipe.vo.StepVO;
import com.foodapp.recipe.vo.SubstituteResultVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 菜谱核心接口控制器。
 * 除 /api/recipe/ugc 需登录外，其余接口公开访问（游客可浏览）。
 */
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final RecipeQueryService recipeQueryService;
    private final SubstituteService substituteService;
    private final UgcService ugcService;

    public RecipeController(RecipeQueryService recipeQueryService,
                            SubstituteService substituteService,
                            UgcService ugcService) {
        this.recipeQueryService = recipeQueryService;
        this.substituteService = substituteService;
        this.ugcService = ugcService;
    }

    /**
     * 分页查询菜谱列表（条件可自由组合；ingredients 逗号分隔食材名即"冰箱清理模式"）。
     */
    @GetMapping("/list")
    public Result<PageResultVO<RecipeSummaryVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer cuisineType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal maxCalories,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String ingredients) {
        return Result.success(recipeQueryService.pageQuery(
                page, size, cuisineType, difficulty, keyword, maxCalories, tagName, ingredients));
    }

    /**
     * 查询菜谱详情（五步法分组步骤 + 食材清单 + 适宜/慎用标签 + 阶段权重）。
     */
    @GetMapping("/{id}")
    public Result<RecipeDetailVO> detail(@PathVariable Long id) {
        return Result.success(recipeQueryService.detail(id));
    }

    /**
     * 查询菜谱步骤平铺列表（按五步法阶段顺序排序，供厨房引擎服务调用）。
     */
    @GetMapping("/{id}/steps")
    public Result<List<StepVO>> steps(@PathVariable Long id) {
        return Result.success(recipeQueryService.flatSteps(id));
    }

    /**
     * 智能食材替换：按场景返回替换建议与替换前后热量对比。
     */
    @PostMapping("/{id}/substitute")
    public Result<SubstituteResultVO> substitute(@PathVariable Long id,
                                                 @Valid @RequestBody SubstituteRequest request) {
        return Result.success(substituteService.substitute(id, request.getScene()));
    }

    /**
     * 菜谱浏览数 +1（进入详情页时调用，原子更新）。
     */
    @PostMapping("/{id}/view")
    public Result<Void> view(@PathVariable Long id) {
        recipeQueryService.increaseView(id);
        return Result.success();
    }

    /**
     * UGC 菜谱上传（需登录；五步法模板强校验；入库为待审核状态）。
     */
    @PostMapping("/ugc")
    public Result<Map<String, Long>> uploadUgc(@Valid @RequestBody UgcRecipeRequest request) {
        Long userId = UserContext.requireUserId();
        Long recipeId = ugcService.createUgcRecipe(userId, request);
        return Result.success("菜谱已提交审核", Map.of("id", recipeId));
    }
}

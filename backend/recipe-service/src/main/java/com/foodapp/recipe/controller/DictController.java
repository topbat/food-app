package com.foodapp.recipe.controller;

import com.foodapp.common.result.Result;
import com.foodapp.recipe.entity.RecipeIngredientLib;
import com.foodapp.recipe.entity.RecipeTag;
import com.foodapp.recipe.repository.RecipeIngredientLibRepository;
import com.foodapp.recipe.repository.RecipeTagRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典接口控制器：食材库与标签字典（公开访问，前端搜索页/UGC上传页使用）。
 */
@RestController
@RequestMapping("/api/recipe")
public class DictController {

    private final RecipeIngredientLibRepository ingredientLibRepository;
    private final RecipeTagRepository tagRepository;

    public DictController(RecipeIngredientLibRepository ingredientLibRepository,
                          RecipeTagRepository tagRepository) {
        this.ingredientLibRepository = ingredientLibRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * 查询全量食材库（按分类排序，含每100克营养成分与禁忌提示）。
     */
    @GetMapping("/ingredient/list")
    public Result<List<RecipeIngredientLib>> ingredientList() {
        return Result.success(ingredientLibRepository.findAllByOrderByCategoryAscIdAsc());
    }

    /**
     * 查询全量标签字典（1人群 2功效 3场景）。
     */
    @GetMapping("/tag/list")
    public Result<List<RecipeTag>> tagList() {
        return Result.success(tagRepository.findAllByOrderByTagTypeAscIdAsc());
    }
}

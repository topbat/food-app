package com.foodapp.user.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.user.dto.AddTagRequest;
import com.foodapp.user.service.TagService;
import com.foodapp.user.vo.TagVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Z世代标签接口：我的标签列表、添加标签、删除标签（全部需登录）。
 */
@RestController
@RequestMapping("/api/user/tag")
public class TagController {

    private final TagService tagService;

    /**
     * 构造注入标签业务层。
     *
     * @param tagService 标签业务层
     */
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 我的标签列表。
     *
     * @return 标签VO列表
     */
    @GetMapping("/list")
    public Result<List<TagVO>> listTags() {
        Long userId = UserContext.requireUserId();
        return Result.success(tagService.listTags(userId));
    }

    /**
     * 添加标签。
     *
     * @param request 添加入参（tagName/tagType）
     * @return 新建标签VO
     */
    @PostMapping
    public Result<TagVO> addTag(@Valid @RequestBody AddTagRequest request) {
        Long userId = UserContext.requireUserId();
        return Result.success("标签添加成功", tagService.addTag(userId, request));
    }

    /**
     * 删除标签（仅能删除本人标签）。
     *
     * @param id 标签ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        tagService.deleteTag(userId, id);
        return Result.success("标签删除成功", null);
    }
}

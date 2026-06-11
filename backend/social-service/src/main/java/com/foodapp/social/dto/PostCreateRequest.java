package com.foodapp.social.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 发帖请求体。
 */
public class PostCreateRequest {

    /** 关联菜谱ID（可空，晒装盘时关联） */
    private Long recipeId;

    /** 帖子文字内容 */
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 1000, message = "帖子内容最长1000字")
    private String content;

    /** 图片URL列表 */
    private List<String> imageUrls;

    /** 帖子类型（1作品晒图 2烹饪打卡 3美食日记） */
    @NotNull(message = "帖子类型不能为空")
    @Min(value = 1, message = "帖子类型仅支持1/2/3")
    @Max(value = 3, message = "帖子类型仅支持1/2/3")
    private Integer postType;

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public Integer getPostType() { return postType; }
    public void setPostType(Integer postType) { this.postType = postType; }
}

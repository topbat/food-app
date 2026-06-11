package com.foodapp.search.vo;

import java.util.List;
import java.util.Map;

/**
 * 个性化推荐响应视图对象（契约：{reason,list:[RecipeSummary]}）。
 * list 项为菜谱服务返回的 RecipeSummary 原样透传，本服务不做字段裁剪。
 */
public class RecommendVO {

    /** 推荐理由文案（如"根据你的标签【减脂期、健身后】为你推荐"） */
    private String reason;
    /** 推荐菜谱列表（RecipeSummary 透传） */
    private List<Map<String, Object>> list;

    public RecommendVO() {
    }

    public RecommendVO(String reason, List<Map<String, Object>> list) {
        this.reason = reason;
        this.list = list;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<Map<String, Object>> getList() { return list; }
    public void setList(List<Map<String, Object>> list) { this.list = list; }
}

package com.foodapp.social.service;

import com.foodapp.social.dto.ShareCardRequest;
import com.foodapp.social.dto.ShareCardVO;
import com.foodapp.social.dto.UserPublicVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 分享卡数据服务。
 * 按契约组装 {title,subtitle,calorieText,equivalentText,continuousDays,nickname,date}，
 * 运动等效文案"相当于慢跑X分钟"按 X = 热量(千卡) / 11.6 四舍五入计算。
 */
@Service
public class ShareCardService {

    private static final Logger log = LoggerFactory.getLogger(ShareCardService.class);

    /** 慢跑每分钟消耗热量（千卡/分钟），用于运动等效换算 */
    private static final double JOGGING_KCAL_PER_MINUTE = 11.6;

    /** 分享卡日期格式 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserInfoService userInfoService;
    private final CheckinService checkinService;

    /**
     * 构造注入用户信息服务（取昵称）与打卡服务（取当前连续打卡天数）。
     *
     * @param userInfoService 用户公开信息服务
     * @param checkinService  打卡服务
     */
    public ShareCardService(UserInfoService userInfoService, CheckinService checkinService) {
        this.userInfoService = userInfoService;
        this.checkinService = checkinService;
    }

    /**
     * 组装分享卡数据：
     * title 按是否携带菜谱名生成；caloriesKcal 非空时生成热量文案与慢跑等效文案；
     * continuousDays 取当前连续打卡天数；nickname 走用户服务（带降级）；date 为今天 yyyy-MM-dd。
     *
     * @param userId  当前登录用户ID
     * @param request 分享卡请求体（recipeId/recipeName/caloriesKcal 均可空）
     * @return 分享卡数据 VO
     */
    public ShareCardVO buildCard(Long userId, ShareCardRequest request) {
        ShareCardVO vo = new ShareCardVO();
        // 关键判断：有菜谱名时生成作品向文案，否则生成通用打卡文案
        if (request.getRecipeName() != null && !request.getRecipeName().isBlank()) {
            vo.setTitle("我做了「" + request.getRecipeName() + "」");
            vo.setSubtitle("食研社 · 用心做好每一餐");
        } else {
            vo.setTitle("今日烹饪打卡");
            vo.setSubtitle("食研社 · 健康饮食每一天");
        }
        // 关键判断：携带热量时生成热量文案与慢跑等效文案（X = 热量 / 11.6 四舍五入）
        if (request.getCaloriesKcal() != null) {
            long kcal = Math.round(request.getCaloriesKcal());
            long joggingMinutes = Math.round(request.getCaloriesKcal() / JOGGING_KCAL_PER_MINUTE);
            vo.setCalorieText("本餐热量约" + kcal + "千卡");
            vo.setEquivalentText("相当于慢跑" + joggingMinutes + "分钟");
        }
        vo.setContinuousDays(checkinService.currentContinuousDays(userId));
        UserPublicVO user = userInfoService.getUser(userId);
        vo.setNickname(user.getNickname());
        vo.setDate(LocalDate.now().format(DATE_FORMATTER));
        log.info("[分享卡] 用户{}生成分享卡, recipeName={}, caloriesKcal={}, continuousDays={}",
                userId, request.getRecipeName(), request.getCaloriesKcal(), vo.getContinuousDays());
        return vo;
    }
}

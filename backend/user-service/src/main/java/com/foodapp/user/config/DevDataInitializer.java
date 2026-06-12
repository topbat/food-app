package com.foodapp.user.config;

import com.foodapp.user.entity.UserAccount;
import com.foodapp.user.entity.UserHealthProfile;
import com.foodapp.user.entity.UserTag;
import com.foodapp.user.repository.UserAccountRepository;
import com.foodapp.user.repository.UserHealthProfileRepository;
import com.foodapp.user.repository.UserTagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * dev 环境种子数据初始化器。
 * 启动时若演示账号不存在，则创建 demo/123456 演示用户及配套健康档案与两个画像标签，
 * 并创建 6 个社区演示用户（ID 2-7，密码同为 123456），与 social-service 种子帖子的
 * user_id 一一对应，保证社区列表能展示真实昵称与头像。仅在 dev 环境生效。
 */
@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    /** 社区演示用户：用户名 / 昵称 / 头像（前端静态资源） */
    private static final String[][] COMMUNITY_USERS = {
            {"chenpopo",  "陈皮婆婆",   "/images/avatars/u2.svg"},
            {"latiaoxiong", "辣条熊",   "/images/avatars/u3.svg"},
            {"qingcai",   "青菜不青",   "/images/avatars/u4.svg"},
            {"feixia",    "翻锅飞侠",   "/images/avatars/u5.svg"},
            {"tangyuaner", "汤圆儿",    "/images/avatars/u6.svg"},
            {"laowangshu", "隔壁老王叔", "/images/avatars/u7.svg"},
    };

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private final UserAccountRepository accountRepository;
    private final UserHealthProfileRepository profileRepository;
    private final UserTagRepository tagRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 构造注入依赖。
     *
     * @param accountRepository 账号仓库
     * @param profileRepository 健康档案仓库
     * @param tagRepository     标签仓库
     * @param passwordEncoder   BCrypt 密码加密器
     */
    public DevDataInitializer(UserAccountRepository accountRepository,
                              UserHealthProfileRepository profileRepository,
                              UserTagRepository tagRepository,
                              BCryptPasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.tagRepository = tagRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 启动时执行：若 demo 用户不存在则创建演示账号、健康档案与标签。
     *
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        // 关键判断：演示账号已存在则跳过，避免重复创建
        if (accountRepository.existsByUsername("demo")) {
            log.info("[种子数据] 演示账号demo已存在，跳过初始化");
            return;
        }

        UserAccount account = new UserAccount();
        account.setUsername("demo");
        account.setPassword(passwordEncoder.encode("123456"));
        account.setNickname("美食家小研");
        account.setAvatarUrl("/images/avatars/u1.svg");
        accountRepository.save(account);

        UserHealthProfile profile = new UserHealthProfile();
        profile.setUserId(account.getId());
        profile.setHeightCm(new BigDecimal("170.0"));
        profile.setWeightKg(new BigDecimal("65.0"));
        profile.setHealthGoal("减脂");
        profile.setDailyCalorieTarget(1800);
        profile.setAllergyHistory("海鲜");
        profileRepository.save(profile);

        UserTag crowdTag = new UserTag();
        crowdTag.setUserId(account.getId());
        crowdTag.setTagName("减脂期");
        crowdTag.setTagType(1);
        tagRepository.save(crowdTag);

        UserTag stateTag = new UserTag();
        stateTag.setUserId(account.getId());
        stateTag.setTagName("健身后");
        stateTag.setTagType(2);
        tagRepository.save(stateTag);

        log.info("[种子数据] 演示账号已创建: username=demo, userId={}, 档案与标签已就绪", account.getId());

        // 社区演示用户：按声明顺序依次创建，自增ID依次为 2-7，
        // 与 social-service data.sql 中帖子/评论的 user_id 对应
        for (String[] u : COMMUNITY_USERS) {
            UserAccount communityUser = new UserAccount();
            communityUser.setUsername(u[0]);
            communityUser.setPassword(passwordEncoder.encode("123456"));
            communityUser.setNickname(u[1]);
            communityUser.setAvatarUrl(u[2]);
            accountRepository.save(communityUser);
            log.info("[种子数据] 社区演示用户已创建: username={}, userId={}", u[0], communityUser.getId());
        }
    }
}

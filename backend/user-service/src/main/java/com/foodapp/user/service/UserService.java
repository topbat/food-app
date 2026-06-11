package com.foodapp.user.service;

import com.foodapp.common.auth.JwtUtil;
import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.user.dto.LoginRequest;
import com.foodapp.user.dto.RegisterRequest;
import com.foodapp.user.dto.UpdateProfileRequest;
import com.foodapp.user.entity.UserAccount;
import com.foodapp.user.entity.UserHealthProfile;
import com.foodapp.user.entity.UserTag;
import com.foodapp.user.repository.UserAccountRepository;
import com.foodapp.user.repository.UserHealthProfileRepository;
import com.foodapp.user.repository.UserTagRepository;
import com.foodapp.user.vo.AuthVO;
import com.foodapp.user.vo.ProfileVO;
import com.foodapp.user.vo.PublicUserVO;
import com.foodapp.user.vo.TagVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户账号业务层：注册、登录、公开信息、个人主页、健康档案更新。
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserAccountRepository accountRepository;
    private final UserHealthProfileRepository profileRepository;
    private final UserTagRepository tagRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 构造注入依赖。
     *
     * @param accountRepository 账号仓库
     * @param profileRepository 健康档案仓库
     * @param tagRepository     标签仓库
     * @param passwordEncoder   BCrypt 密码加密器
     * @param jwtUtil           JWT 工具
     */
    public UserService(UserAccountRepository accountRepository,
                       UserHealthProfileRepository profileRepository,
                       UserTagRepository tagRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.tagRepository = tagRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册：用户名唯一校验 → BCrypt 加密落库 → 自动创建空健康档案 → 签发 Token。
     *
     * @param request 注册入参（用户名/密码/昵称）
     * @return token + 用户基础信息
     * @throws BusinessException 用户名已存在时抛 40900
     */
    @Transactional
    public AuthVO register(RegisterRequest request) {
        // 关键判断：用户名唯一校验，重复注册直接拒绝
        if (accountRepository.existsByUsername(request.getUsername())) {
            log.warn("[注册] 用户名已存在，拒绝注册: username={}", request.getUsername());
            throw new BusinessException(ResultCode.BIZ_CONFLICT, "用户名已存在");
        }
        UserAccount account = new UserAccount();
        account.setUsername(request.getUsername());
        // 密码 BCrypt 加密存储，绝不落明文、绝不打日志
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setNickname(request.getNickname());
        accountRepository.save(account);

        // 注册成功自动创建空健康档案，便于后续直接更新
        UserHealthProfile profile = new UserHealthProfile();
        profile.setUserId(account.getId());
        profileRepository.save(profile);

        log.info("[注册] 注册成功: userId={}, username={}", account.getId(), account.getUsername());
        String token = jwtUtil.generateToken(account.getId(), account.getNickname());
        return new AuthVO(token, toSimpleUser(account));
    }

    /**
     * 用户登录：用户名不存在或密码不匹配统一返回"用户名或密码错误"（不暴露具体哪个错）；禁用账号拒绝登录。
     *
     * @param request 登录入参（用户名/密码）
     * @return token + 用户基础信息
     * @throws BusinessException 用户名或密码错误、账号被禁用时抛出
     */
    public AuthVO login(LoginRequest request) {
        UserAccount account = accountRepository.findByUsername(request.getUsername()).orElse(null);
        // 关键判断：用户名不存在 —— 与密码错误统一提示，避免暴露账号是否存在
        if (account == null) {
            log.warn("[登录] 登录失败（用户名不存在）: username={}", request.getUsername());
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }
        // 关键判断：BCrypt 校验密码是否匹配
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            log.warn("[登录] 登录失败（密码不匹配）: username={}", request.getUsername());
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }
        // 关键判断：账号被禁用（status=0）时拒绝登录
        if (account.getStatus() != null && account.getStatus() == 0) {
            log.warn("[登录] 登录失败（账号已禁用）: username={}", request.getUsername());
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        log.info("[登录] 登录成功: userId={}, username={}", account.getId(), account.getUsername());
        String token = jwtUtil.generateToken(account.getId(), account.getNickname());
        return new AuthVO(token, toSimpleUser(account));
    }

    /**
     * 查询用户公开信息（供社交服务与前端展示，仅返回ID/昵称/头像）。
     *
     * @param id 用户ID
     * @return 公开信息
     * @throws BusinessException 用户不存在时抛 40400
     */
    public PublicUserVO getPublicUser(Long id) {
        UserAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));
        return new PublicUserVO(account.getId(), account.getNickname(), account.getAvatarUrl());
    }

    /**
     * 查询个人主页：用户信息 + 健康档案 + 标签列表。
     *
     * @param userId 当前登录用户ID
     * @return 个人主页聚合数据
     * @throws BusinessException 用户不存在时抛 40400
     */
    public ProfileVO getProfile(Long userId) {
        UserAccount account = accountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        ProfileVO.ProfileUserVO userVO = new ProfileVO.ProfileUserVO();
        userVO.setId(account.getId());
        userVO.setUsername(account.getUsername());
        userVO.setNickname(account.getNickname());
        userVO.setAvatarUrl(account.getAvatarUrl());
        userVO.setGender(account.getGender());
        userVO.setBirthDate(account.getBirthDate());

        ProfileVO.HealthProfileVO profileVO = new ProfileVO.HealthProfileVO();
        UserHealthProfile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile != null) {
            profileVO.setHeightCm(profile.getHeightCm());
            profileVO.setWeightKg(profile.getWeightKg());
            profileVO.setAllergyHistory(profile.getAllergyHistory());
            profileVO.setDietPreference(profile.getDietPreference());
            profileVO.setHealthGoal(profile.getHealthGoal());
            profileVO.setDailyCalorieTarget(profile.getDailyCalorieTarget());
        }

        List<TagVO> tags = tagRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(this::toTagVO)
                .toList();

        ProfileVO vo = new ProfileVO();
        vo.setUser(userVO);
        vo.setProfile(profileVO);
        vo.setTags(tags);
        return vo;
    }

    /**
     * 更新健康档案（含昵称/头像）：仅更新传入的非空字段；档案不存在时自动补建。
     *
     * @param userId  当前登录用户ID
     * @param request 更新入参（全部字段可选）
     * @throws BusinessException 用户不存在时抛 40400
     */
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        UserAccount account = accountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        // 昵称/头像属于账号表字段
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            account.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            account.setAvatarUrl(request.getAvatarUrl());
        }
        accountRepository.save(account);

        // 关键判断：健康档案不存在（历史数据）时自动补建一份
        UserHealthProfile profile = profileRepository.findByUserId(userId).orElseGet(() -> {
            log.info("[档案] 用户{}健康档案不存在，自动补建", userId);
            UserHealthProfile created = new UserHealthProfile();
            created.setUserId(userId);
            return created;
        });
        if (request.getHeightCm() != null) {
            profile.setHeightCm(request.getHeightCm());
        }
        if (request.getWeightKg() != null) {
            profile.setWeightKg(request.getWeightKg());
        }
        if (request.getAllergyHistory() != null) {
            profile.setAllergyHistory(request.getAllergyHistory());
        }
        if (request.getDietPreference() != null) {
            profile.setDietPreference(request.getDietPreference());
        }
        if (request.getHealthGoal() != null) {
            profile.setHealthGoal(request.getHealthGoal());
        }
        if (request.getDailyCalorieTarget() != null) {
            profile.setDailyCalorieTarget(request.getDailyCalorieTarget());
        }
        profileRepository.save(profile);
        log.info("[档案] 用户{}健康档案更新成功", userId);
    }

    /**
     * 实体转注册/登录用基础用户VO（隔离密码字段）。
     *
     * @param account 账号实体
     * @return 基础用户VO
     */
    private AuthVO.SimpleUserVO toSimpleUser(UserAccount account) {
        return new AuthVO.SimpleUserVO(account.getId(), account.getUsername(),
                account.getNickname(), account.getAvatarUrl());
    }

    /**
     * 标签实体转VO。
     *
     * @param tag 标签实体
     * @return 标签VO
     */
    private TagVO toTagVO(UserTag tag) {
        return new TagVO(tag.getId(), tag.getTagName(), tag.getTagType());
    }
}

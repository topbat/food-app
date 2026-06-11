package com.foodapp.user.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.user.dto.LoginRequest;
import com.foodapp.user.dto.RegisterRequest;
import com.foodapp.user.dto.UpdateProfileRequest;
import com.foodapp.user.service.UserService;
import com.foodapp.user.vo.AuthVO;
import com.foodapp.user.vo.ProfileVO;
import com.foodapp.user.vo.PublicUserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户账号接口：注册、登录、公开信息、个人主页、健康档案更新。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    /**
     * 构造注入用户业务层。
     *
     * @param userService 用户业务层
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 注册（白名单接口）。
     *
     * @param request 注册入参（username/password/nickname）
     * @return token + 用户基础信息
     */
    @PostMapping("/register")
    public Result<AuthVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", userService.register(request));
    }

    /**
     * 登录（白名单接口）。
     *
     * @param request 登录入参（username/password）
     * @return token + 用户基础信息
     */
    @PostMapping("/login")
    public Result<AuthVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", userService.login(request));
    }

    /**
     * 用户公开信息（白名单接口，供社交服务/前端展示）。
     *
     * @param id 用户ID
     * @return id/nickname/avatarUrl
     */
    @GetMapping("/public/{id}")
    public Result<PublicUserVO> getPublicUser(@PathVariable Long id) {
        return Result.success(userService.getPublicUser(id));
    }

    /**
     * 个人主页（需登录）：用户信息 + 健康档案 + 标签列表。
     *
     * @return 个人主页聚合数据
     */
    @GetMapping("/profile")
    public Result<ProfileVO> getProfile() {
        Long userId = UserContext.requireUserId();
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 更新健康档案（需登录）：昵称/头像/身高/体重/过敏史/饮食偏好/健康目标/热量目标，均可选。
     *
     * @param request 更新入参
     * @return 成功响应
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = UserContext.requireUserId();
        userService.updateProfile(userId, request);
        return Result.success("健康档案更新成功", null);
    }
}

package com.foodapp.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 用户与画像服务启动类（端口 8081）。
 * 同时扫描 common 公共包与本服务包，保证统一响应/异常/JWT/日志组件生效。
 */
@SpringBootApplication(scanBasePackages = {"com.foodapp.common", "com.foodapp.user"})
public class UserServiceApplication {

    /**
     * 服务入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    /**
     * 密码加密器：BCrypt 单向加密，用于注册时加密与登录时校验，禁止明文存储密码。
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

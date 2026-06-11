package com.foodapp.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 社交与UGC服务启动类（端口 8085）。
 * 同时扫描 common 公共包与本服务包，保证统一响应/异常/JWT/日志组件生效。
 */
@SpringBootApplication(scanBasePackages = {"com.foodapp.common", "com.foodapp.social"})
@EnableJpaRepositories(basePackages = "com.foodapp.social.repository")
@EntityScan(basePackages = "com.foodapp.social.entity")
public class SocialServiceApplication {

    /**
     * 服务入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SocialServiceApplication.class, args);
    }
}

package com.foodapp.recipe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 菜谱结构化服务启动类（全系统核心，端口 8082）。
 * 扫描 common 公共组件与本服务业务包；实体与仓库均位于 com.foodapp.recipe 子包。
 */
@SpringBootApplication(scanBasePackages = {"com.foodapp.common", "com.foodapp.recipe"})
@EnableJpaRepositories(basePackages = "com.foodapp.recipe.repository")
@EntityScan(basePackages = "com.foodapp.recipe.entity")
public class RecipeServiceApplication {

    /**
     * 服务入口：启动 Spring Boot 应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RecipeServiceApplication.class, args);
    }
}

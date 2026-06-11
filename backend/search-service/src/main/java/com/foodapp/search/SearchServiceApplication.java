package com.foodapp.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 智能搜索推荐服务启动类（端口 8084）。
 * 同时扫描 common 公共包与本服务包，保证统一响应/异常/JWT/日志组件生效。
 * 职责：多维检索透传（HTTP 调菜谱服务）、热搜榜、搜索历史、个性化推荐（HTTP 调用户服务取标签）。
 */
@SpringBootApplication(scanBasePackages = {"com.foodapp.common", "com.foodapp.search"})
@EnableJpaRepositories(basePackages = "com.foodapp.search.repository")
@EntityScan(basePackages = "com.foodapp.search.entity")
public class SearchServiceApplication {

    /**
     * 服务入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}

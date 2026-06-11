package com.foodapp.kitchen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 厨房引擎服务启动类（:8083）。
 * 职责：烹饪会话五步法状态机、多任务并行倒计时、语音指令解析。
 * 开启定时任务（@EnableScheduling）用于计时器到点的兜底扫描（模拟推送）。
 */
@SpringBootApplication(scanBasePackages = {"com.foodapp.common", "com.foodapp.kitchen"})
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.foodapp.kitchen.repository")
@EntityScan(basePackages = "com.foodapp.kitchen.entity")
public class KitchenServiceApplication {

    /**
     * 服务入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(KitchenServiceApplication.class, args);
    }
}

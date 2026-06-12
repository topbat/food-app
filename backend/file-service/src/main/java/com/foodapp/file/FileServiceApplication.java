package com.foodapp.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文件存储服务启动类（端口 8087）。
 * 职责：对象存储上传（MinIO / 阿里云 OSS 策略切换）、图片/视频缩略图生成、上传记录管理。
 * 同时扫描 common 公共包与本服务包，保证统一响应/异常/JWT/日志组件生效。
 */
@SpringBootApplication(scanBasePackages = {"com.foodapp.common", "com.foodapp.file"})
public class FileServiceApplication {

    /**
     * 服务入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}

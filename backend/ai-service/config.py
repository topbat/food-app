# -*- coding: utf-8 -*-
"""
配置模块：按环境变量 APP_ENV（dev/uat/prod，默认 dev）加载多环境配置。

约定（与项目总体规划一致）：
- dev ：日志级别 DEBUG，CORS 仅放行前端 http://localhost:5173，开启热重载
- uat ：日志级别 INFO，CORS 放行 uat 前端来源，关闭热重载
- prod：日志级别 INFO，CORS 放行生产前端来源，关闭热重载
"""

import os
import logging
from logging.handlers import RotatingFileHandler


class Settings:
    """配置对象：根据当前环境提供日志级别、CORS 来源、热重载开关等配置项。"""

    # 服务基本信息（所有环境一致）
    APP_NAME = "ai-service"
    HOST = "0.0.0.0"
    PORT = 8086

    # 各环境差异化配置表
    _ENV_CONFIG = {
        "dev": {
            "log_level": "DEBUG",                          # 开发环境输出 DEBUG 日志，便于调试
            "cors_origins": ["http://localhost:5173"],     # 仅放行本地前端（安全要求）
            "reload": True,                                # 开发环境开启热重载
        },
        "uat": {
            "log_level": "INFO",
            "cors_origins": ["http://uat.foodapp.local"],  # UAT 前端来源（按实际部署调整）
            "reload": False,
        },
        "prod": {
            "log_level": "INFO",
            "cors_origins": ["https://www.foodapp.com"],   # 生产前端来源（按实际部署调整）
            "reload": False,
        },
    }

    def __init__(self):
        """读取环境变量 APP_ENV 初始化配置，非法值回退为 dev。"""
        env = os.getenv("APP_ENV", "dev").lower()
        if env not in self._ENV_CONFIG:
            env = "dev"
        self.env = env
        cfg = self._ENV_CONFIG[env]
        self.log_level = cfg["log_level"]
        self.cors_origins = cfg["cors_origins"]
        self.reload = cfg["reload"]


# 全局唯一配置实例（导入即生效）
settings = Settings()


def setup_logging() -> logging.Logger:
    """
    初始化全局日志：同时输出到控制台与 logs/ai-service.log。

    - 滚动文件：RotatingFileHandler，单文件最大 10MB，保留 5 个备份
    - 格式：时间 / 级别 / 模块名 / 消息
    - 级别按当前环境配置（dev=DEBUG，uat/prod=INFO）
    """
    # 日志目录放在 ai-service 自身目录下，避免受启动路径影响
    base_dir = os.path.dirname(os.path.abspath(__file__))
    log_dir = os.path.join(base_dir, "logs")
    os.makedirs(log_dir, exist_ok=True)

    formatter = logging.Formatter(
        fmt="%(asctime)s [%(levelname)s] [%(name)s] %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    root = logging.getLogger()
    root.setLevel(settings.log_level)

    # 防止重复初始化导致日志重复输出
    if not root.handlers:
        # 控制台输出
        console = logging.StreamHandler()
        console.setFormatter(formatter)
        root.addHandler(console)

        # 滚动文件输出：10MB × 5
        file_handler = RotatingFileHandler(
            os.path.join(log_dir, "ai-service.log"),
            maxBytes=10 * 1024 * 1024,
            backupCount=5,
            encoding="utf-8",
        )
        file_handler.setFormatter(formatter)
        root.addHandler(file_handler)

    return logging.getLogger(settings.APP_NAME)

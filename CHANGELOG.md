# 变更历史（CHANGELOG）

> **提交规范**：后续每一次 git 提交，必须同步在本文件最上方追加一条变更记录，格式如下：
>
> ```
> ## [日期] 简短标题
> - 变更类型（新增/修复/优化/文档/重构）：具体改动内容
> - 影响范围：涉及的模块（user/recipe/kitchen/search/social/ai/frontend/docs）
> ```
>
> 未附带变更记录的提交视为不合规提交。

---

## [2026-06-12] 提交规范与文档完善

- 新增：建立变更历史提交规范，新增本 CHANGELOG.md 文件
- 文档：README 启动章节补充「一键启动（dev 环境，免装 MySQL）」命令块
- 影响范围：docs

## [2026-06-11] 中文乱码与 CORS 修复

- 修复：PowerShell 控制台中文乱码 —— 新增 `backend/start-dev.ps1` 一键启动脚本（chcp 65001 UTF-8 控制台）；logback 控制台编码改为 `${CONSOLE_LOG_CHARSET:-UTF-8}` 可覆盖参数
- 修复：5 个 Java 服务响应强制 `Content-Type: application/json;charset=UTF-8`（`server.servlet.encoding.force: true`）
- 修复：CORS 改用 OriginPatterns，默认放行 `http://localhost:5173`、`http://127.0.0.1:5173`（及 4173 预览端口），解决 `127.0.0.1` 访问被 403 Invalid CORS request 拒绝的问题
- 影响范围：common、user、recipe、kitchen、search、social、docs

## [2026-06-11] 初始提交：菜谱助手「食研社」全栈实现

- 新增：多模块微服务后端 —— 用户 8081 / 菜谱 8082 / 厨房 8083 / 搜索 8084 / 社交 8085（Spring Boot 3.3.5 + Java 21，表前缀区分模块，dev/uat/prod 三环境默认 dev，方法中文 Javadoc + 关键逻辑日志）
- 新增：AI 服务 8086（Python FastAPI —— 智能食材替换、自由文本五步法结构化解析）
- 新增：前端 H5/PC 响应式（React 18 + Vite + Tailwind + Zustand，新中式视觉；厨房沉浸模式：五阶段进度/多并行计时器/语音指令/防误触/屏幕常亮；社区/打卡/徽章/饮食日历/UGC 五步法上传）
- 新增：MySQL 建表脚本（表/字段中文注释，sql/00~05）、6 个模块测试指南、前端测试指南、联调测试指南、README
- 影响范围：全部模块

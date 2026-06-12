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

## [2026-06-12] 对象存储文件服务与前端暗黑模式

- 新增：文件存储服务 `file-service`（端口 **8087**，表前缀 `file_`）—— 对象存储策略模式（**MinIO 默认实现** / 阿里云 OSS 可配置切换，`foodapp.storage.type=minio|aliyun`），bucket 自动创建并设公共读；MinIO 未启动时服务可正常运行、上传返回友好 50000 提示
- 新增：图片/视频上传统一接口 `POST /api/file/upload`（图片 jpg/png/webp/gif ≤10MB，视频 mp4/mov ≤100MB），上传同时自动生成 **400px 宽预览缩略图**（图片缩放 / 视频抓首帧），缩略图地址规则：原 URL 去扩展名 + `_thumb.jpg`，供首页/列表页小图显示提升加载性能
- 新增：`sql/06_file.sql` 建表脚本、file-service 测试指南（含 MinIO 真实上传/缩略图实测记录）
- 新增：前端**暗黑模式** —— 色彩 token 全面 CSS 变量化（`darkMode: 'class'`），主题三态切换（亮/暗/跟随系统，localStorage 持久化，防首屏闪白）；暗色采用暖墨黑底 + 沉朱砂 + 青玉 + 暗夜琥珀金，亮色新增金棕点缀提升文化质感
- 新增：前端上传接入 —— 发帖多图本地上传（带进度）、UGC 封面与步骤图片/视频上传、个人头像上传；列表/网格统一使用 `SmartImage` 组件（缩略图优先 + 原图回退 + 懒加载）
- 文档：接口契约新增文件服务一节；README / 项目总体规划 端口表与启动步骤（MinIO 两种启动方式）同步更新
- 影响范围：file（新模块）、frontend、docs、sql

## [2026-06-12] 修复 PowerShell 脚本编码

- 修复：`scripts/deploy.ps1`、`backend/start-dev.ps1` 增加 UTF-8 BOM —— Windows PowerShell 5.1 将无 BOM 的 UTF-8 脚本按 GBK 解析，中文注释会导致 ParserError
- 影响范围：scripts、backend

## [2026-06-12] 分支部署规范与 Actions 部署流水线

- 新增：三分支部署规范（`dev`→dev 环境、`master`→uat 环境、`main`→prod 环境，合并流向 dev→master→main），详见 部署规范.md
- 新增：GitHub Actions 部署流水线 `.github/workflows/deploy.yml` —— 分支推送自动触发 + `workflow_dispatch` 手动触发（gh CLI 脚本 / GitHub 网页 Actions 页），解析环境→并行构建（后端 Maven / 前端 npm / AI 自检）→模拟发布，产物保留 7 天
- 新增：部署触发脚本 `scripts/deploy.ps1`（按分支/环境触发，支持 -Watch 实时跟踪）
- 文档：README 增加部署规范与变更历史链接
- 影响范围：docs、CI/CD

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

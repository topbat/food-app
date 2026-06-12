# 菜谱助手「食研社」

面向 Z 世代的精细化、可视化、健康化中式烹饪助手。采用多模块微服务架构：5 个 Java（Spring Boot）业务服务 + 1 个 Python（FastAPI）AI 服务 + React 前端。

## 一、架构图

```
┌─────────────────────────────────────────────────────┐
│        前端 H5/PC (React + Vite)  :5173              │
│        （Vite 代理统一转发 /api/* 到各微服务）          │
└──────┬───────┬───────┬───────┬───────┬───────┬──────┘
       │       │       │       │       │       │
   ┌───▼──┐ ┌──▼───┐ ┌─▼────┐ ┌▼─────┐ ┌▼─────┐ ┌▼─────┐
   │ 用户  │ │ 菜谱  │ │ 厨房  │ │ 搜索  │ │ 社交  │ │ AI   │
   │ 8081 │ │ 8082 │ │ 8083 │ │ 8084 │ │ 8085 │ │ 8086 │
   └───┬──┘ └──┬───┘ └─┬────┘ └┬─────┘ └┬─────┘ └──────┘
       │       │       │(HTTP调菜谱)│(HTTP调菜谱/用户)│
   ┌───▼───────▼───────▼───────▼───────▼─────────────┐
   │  MySQL 8.0 (uat/prod) / H2-MySQL模式 (dev)        │
   │  表前缀: user_ recipe_ kitchen_ search_ social_   │
   └──────────────────────────────────────────────────┘
```

跨服务调用关系（HTTP）：

- 厨房服务 → 菜谱服务（开始烹饪时拉取步骤 `/api/recipe/{id}/steps`）
- 搜索服务 → 菜谱服务（搜索透传 `/api/recipe/list`）、→ 用户服务（个性化推荐取标签）
- 社交服务 → 用户服务（补充昵称头像 `/api/user/public/{id}`，内存缓存 10 分钟）

## 二、模块与端口总表

| 模块 | 目录 | 端口 | 表前缀 | 职责 |
|---|---|---|---|---|
| 公共模块 | `backend/common` | — | — | 统一响应 `Result<T>`、JWT 工具、鉴权拦截器、全局异常、访问日志切面 |
| 用户与画像服务 | `backend/user-service` | **8081** | `user_` | 注册登录、JWT 鉴权、健康档案、Z 世代标签、饮食日历 |
| 菜谱结构化服务（核心） | `backend/recipe-service` | **8082** | `recipe_` | 菜谱五步法 CRUD、食材库、营养计算、智能替换、UGC 上传 |
| 厨房引擎服务（高频） | `backend/kitchen-service` | **8083** | `kitchen_` | 烹饪会话状态机、进度计算、多任务倒计时、语音指令解析 |
| 智能搜索推荐服务 | `backend/search-service` | **8084** | `search_` | 多维检索（关键词/食材/营养/人群）、个性化推荐、热搜、搜索历史 |
| 社交与 UGC 服务 | `backend/social-service` | **8085** | `social_` | 评论、作品墙、点赞、评分、打卡、成就徽章、分享卡 |
| AI 服务（Python FastAPI） | `backend/ai-service` | **8086** | — | 智能食材替换建议（规则引擎）、菜谱自由文本五步法结构化解析 |
| 文件存储服务 | `backend/file-service` | **8087** | `file_` | 对象存储上传（MinIO/阿里云OSS策略切换）、图片/视频缩略图生成、上传记录 |
| 前端 H5/PC | `frontend` | **5173** | — | 移动优先 + PC 响应式，新中式美学视觉 |

接口前缀约定：各服务统一为 `/api/<模块>`（如 `/api/user/**`、`/api/recipe/**`、`/api/ai/**`）。

统一响应体：`{"code":0,"message":"操作成功","data":...}`；错误码：40000 参数错误 / 40100 未登录 / 40900 业务冲突 / 50000 服务错误。详见 [接口契约.md](接口契约.md)。

## 三、技术栈

| 层 | 技术 |
|---|---|
| Java 后端 | Spring Boot 3.3.5、Java 21、Spring Data JPA、JWT（HS256）、BCrypt、Maven 多模块 |
| AI 服务 | Python 3.10+（实测 3.14）、FastAPI、Uvicorn、Pydantic v2 |
| 数据库 | dev：H2 内存库（MODE=MySQL，免安装）；uat/prod：MySQL 8.0（单库 `food_app`，表前缀区分模块） |
| 前端 | React 18、Vite 5、Tailwind CSS 3、Zustand、React Router 6、Axios |

## 四、目录结构

```
food-app/
├── backend/
│   ├── pom.xml                 # Maven 父工程（聚合各 Java 模块）
│   ├── common/                 # 公共模块：result/auth/exception/log/config
│   ├── user-service/           # 用户服务 :8081
│   ├── recipe-service/         # 菜谱服务 :8082
│   ├── kitchen-service/        # 厨房服务 :8083
│   ├── search-service/         # 搜索服务 :8084
│   ├── social-service/         # 社交服务 :8085
│   ├── file-service/           # 文件服务 :8087（MinIO/OSS 上传 + 缩略图）
│   └── ai-service/             # AI 服务 :8086（Python FastAPI）
│       ├── main.py             # 入口（接口 + 统一响应 + 全局异常）
│       ├── config.py           # 多环境配置 + 日志初始化
│       ├── services/           # 替换规则引擎、五步法文本解析器
│       └── 测试指南.md
├── frontend/                   # React 前端 :5173
│   └── src/{api,components,pages,store,utils}
├── sql/                        # MySQL 正式 DDL（uat/prod 用）
│   ├── 00_database.sql         # 建库 food_app
│   ├── 01_user.sql ~ 06_file.sql     # 按模块编号
├── 接口契约.md                  # 全部接口定义（开发必须遵守）
├── 项目总体规划.md               # 架构与统一约定
├── 产品需求.md / 前端后端技术栈.md
├── 联调测试指南.md               # 端到端串测脚本（本仓库根目录）
└── README.md
```

## 五、三种环境

| 环境 | 切换方式 | 数据库 | 说明 |
|---|---|---|---|
| **dev**（默认） | Java：`spring.profiles.active: dev`；AI：不设或 `APP_ENV=dev` | H2 内存库（MODE=MySQL），免安装 | `ddl-auto: update` 自动建表，启动自动执行 `data.sql` 种子数据；日志 DEBUG；AI 服务热重载开启 |
| **uat** | Java：`--spring.profiles.active=uat`；AI：`$env:APP_ENV='uat'` | MySQL 8.0 `jdbc:mysql://localhost:3306/food_app` | 需先执行 `sql/` 下 00~05 脚本建库建表；日志 INFO |
| **prod** | Java：`--spring.profiles.active=prod`；AI：`$env:APP_ENV='prod'` | MySQL 8.0 | 同 uat，另关闭 SQL 打印、收紧 CORS |

uat/prod 初始化数据库（需本机 MySQL 8.0）：

```powershell
Get-ChildItem d:\self\food-app\sql\*.sql | Sort-Object Name | ForEach-Object { mysql -uroot -p"你的密码" --default-character-set=utf8mb4 -e "source $($_.FullName)" }
```

## 六、完整启动步骤（dev，按依赖顺序）

> 前置：JDK 21、Maven 3.9+、Python 3.10+、Node.js 18+。dev 环境无需安装 MySQL。
> 每个服务开一个独立终端窗口。

**一键启动（dev 环境，免装 MySQL）**

```powershell
cd d:\self\food-app\backend
mvn -N install; mvn -pl common install -DskipTests   # 首次必须
mvn -pl recipe-service spring-boot:run                # 先启菜谱（其他服务依赖它）
# 新窗口依次: user/kitchen/search/social-service
cd backend\ai-service; python -m pip install -r requirements.txt; python -m uvicorn main:app --port 8086
cd frontend; npm install; npm run dev                 # http://localhost:5173
```

**推荐：一键启动脚本（自动处理中文乱码）**

```powershell
cd d:\self\food-app\backend
.\start-dev.ps1                  # 启动全部 5 个 Java 服务（每个独立窗口，UTF-8 控制台）
.\start-dev.ps1 user-service     # 或只启动指定服务
```

> ⚠️ **PowerShell 中文乱码说明**：Windows 中文系统的 PowerShell 默认 GBK 代码页（936），
> 而服务日志为 UTF-8 编码，直接 `mvn spring-boot:run` 会出现「閰嶇疆」之类乱码。
> 解决：用上面的 `start-dev.ps1`，或手动在终端先执行 `chcp 65001` 再启动；
> 文件日志 `backend/logs/*.log` 始终为 UTF-8，可在任意编辑器正常查看；
> 接口响应已强制 `Content-Type: application/json;charset=UTF-8`，浏览器与前端不受影响。

手动逐个启动：

```powershell
# 0. 首次需要先安装父工程与公共模块（任一服务启动前执行一次）
cd d:\self\food-app\backend
mvn -q -DskipTests install -pl common -am

# 1. 菜谱服务（核心，厨房/搜索依赖它，先启动）:8082
mvn -pl recipe-service spring-boot:run

# 2. 用户服务（鉴权与公开信息，社交/搜索依赖它）:8081
mvn -pl user-service spring-boot:run

# 3. 其余 Java 服务（顺序不限）:8083 / 8084 / 8085
mvn -pl kitchen-service spring-boot:run
mvn -pl search-service spring-boot:run
mvn -pl social-service spring-boot:run

# 4. AI 服务 :8086
cd d:\self\food-app\backend\ai-service
pip install -r requirements.txt
python main.py

# 4.5 文件服务 :8087（需要先启动 MinIO，见下方说明；MinIO 未启动时服务也能起，仅上传报 50000）
cd d:\self\food-app\backend
mvn -pl file-service spring-boot:run

# 5. 前端 :5173
cd d:\self\food-app\frontend
npm install
npm run dev
```

**MinIO 启动（文件服务 :8087 的对象存储依赖，两种方式任选）**

```powershell
# 方式一：Docker（需本机 Docker）
docker run -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address ":9001"

# 方式二：Windows 直接下载 minio.exe 运行（免 Docker）
Invoke-WebRequest https://dl.min.io/server/minio/release/windows-amd64/minio.exe -OutFile minio.exe
.\minio.exe server D:\minio-data --console-address ":9001"
```

> 默认账号密码均为 `minioadmin` / `minioadmin`；API 端口 9000，Web 控制台 http://localhost:9001 。
> bucket `food-app` 无需手动创建，文件服务首次上传时会自动创建并设置公共读策略。

启动验证（任一服务起来后均可单独验证）：

```powershell
Invoke-RestMethod http://localhost:8082/api/recipe/list?page=1&size=2   # 菜谱
Invoke-RestMethod http://localhost:8086/api/ai/health                   # AI
# 浏览器访问 http://localhost:5173                                       # 前端
```

## 七、各模块测试指南索引

| 模块 | 测试指南 |
|---|---|
| 用户服务 | [backend/user-service/测试指南.md](backend/user-service/测试指南.md) |
| 菜谱服务 | [backend/recipe-service/测试指南.md](backend/recipe-service/测试指南.md) |
| 厨房服务 | [backend/kitchen-service/测试指南.md](backend/kitchen-service/测试指南.md) |
| 搜索服务 | [backend/search-service/测试指南.md](backend/search-service/测试指南.md) |
| 社交服务 | [backend/social-service/测试指南.md](backend/social-service/测试指南.md) |
| AI 服务 | [backend/ai-service/测试指南.md](backend/ai-service/测试指南.md) |
| 文件服务 | [backend/file-service/测试指南.md](backend/file-service/测试指南.md) |
| 端到端联调 | [联调测试指南.md](联调测试指南.md) |

## 八、提交规范

> **每一次 git 提交，必须同步在 [CHANGELOG.md](CHANGELOG.md) 最上方追加一条变更记录**（日期、标题、变更类型、影响模块），未附带变更记录的提交视为不合规提交。

## 九、其他文档

- [部署规范.md](部署规范.md) — 三分支模型（dev/master/main）与 Actions 部署流水线三种触发方式
- [CHANGELOG.md](CHANGELOG.md) — 变更历史（每次提交必更新）
- [接口契约.md](接口契约.md) — 全部接口请求/响应定义
- [项目总体规划.md](项目总体规划.md) — 架构、端口、统一技术约定
- [产品需求.md](产品需求.md) — 产品功能需求
- [前端后端技术栈.md](前端后端技术栈.md) — 技术选型说明

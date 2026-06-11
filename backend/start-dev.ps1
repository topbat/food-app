# ============================================================
# 菜谱助手后端 dev 环境一键启动脚本（解决 PowerShell 中文乱码）
# 用法：
#   .\start-dev.ps1                       # 启动全部 5 个 Java 服务
#   .\start-dev.ps1 user-service          # 只启动指定服务（可多个）
# 说明：
#   每个服务在独立 PowerShell 窗口中启动，并先执行 chcp 65001
#   把控制台切到 UTF-8 代码页，与 logback 的 UTF-8 输出一致，
#   从而正确显示中文日志。
# ============================================================
param(
    [string[]]$Services = @('recipe-service', 'user-service', 'kitchen-service', 'search-service', 'social-service')
)

$backend = $PSScriptRoot

# 首次使用需先安装父 POM 与 common 模块（已安装则秒过）
Write-Host "[准备] 安装父 POM 与 common 模块到本地仓库..." -ForegroundColor Cyan
& mvn -q -N install -f "$backend\pom.xml"
& mvn -q -pl common install -DskipTests -f "$backend\pom.xml"

foreach ($s in $Services) {
    Write-Host "[启动] $s （新窗口，UTF-8 控制台）" -ForegroundColor Green
    # chcp 65001：控制台切 UTF-8；JAVA_TOOL_OPTIONS 确保 JVM 标准输出也用 UTF-8
    Start-Process powershell -ArgumentList @(
        '-NoExit',
        '-Command',
        "chcp 65001 | Out-Null; " +
        "`$Host.UI.RawUI.WindowTitle = '$s'; " +
        "`$env:JAVA_TOOL_OPTIONS = '-Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8'; " +
        "Set-Location '$backend'; " +
        "mvn -pl $s spring-boot:run"
    )
    # 菜谱服务先启动（厨房/搜索服务依赖它），稍作间隔避免本地仓库并发写冲突
    Start-Sleep -Seconds 3
}

Write-Host ""
Write-Host "全部启动命令已发出。端口：菜谱8082 用户8081 厨房8083 搜索8084 社交8085" -ForegroundColor Yellow
Write-Host "AI 服务请另开窗口： cd ai-service; python -m uvicorn main:app --port 8086" -ForegroundColor Yellow
Write-Host "前端：             cd ..\frontend; npm run dev   （http://localhost:5173）" -ForegroundColor Yellow

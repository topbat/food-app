# ============================================================
# 部署触发脚本：通过 gh CLI 触发 GitHub Actions 部署流水线
# 前置：已执行 gh auth login
# 用法示例：
#   .\scripts\deploy.ps1                              # 默认：dev 分支 → dev 环境
#   .\scripts\deploy.ps1 -Branch master               # master 分支 → uat 环境（自动映射）
#   .\scripts\deploy.ps1 -Branch main                 # main 分支 → prod 环境（自动映射）
#   .\scripts\deploy.ps1 -Branch main -Environment uat # 显式指定环境（覆盖分支映射）
#   .\scripts\deploy.ps1 -Branch dev -Watch           # 触发后实时跟踪运行进度
# ============================================================
param(
    # 触发分支（dev=开发 / master=预发布 / main=生产）
    [ValidateSet('dev', 'master', 'main')]
    [string]$Branch = 'dev',

    # 目标环境（auto=按分支自动映射 dev→dev / master→uat / main→prod）
    [ValidateSet('auto', 'dev', 'uat', 'prod')]
    [string]$Environment = 'auto',

    # 触发后实时跟踪运行日志
    [switch]$Watch
)

$repo = 'topbat/food-app'

Write-Host "[部署] 触发流水线：分支=$Branch 环境=$Environment 仓库=$repo" -ForegroundColor Cyan
gh workflow run deploy.yml --repo $repo --ref $Branch -f environment=$Environment
if ($LASTEXITCODE -ne 0) {
    Write-Host "[失败] 触发失败，请确认已 gh auth login 且分支已推送到远程" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 5
Write-Host "[部署] 最近 3 次运行：" -ForegroundColor Cyan
gh run list --repo $repo --workflow=deploy.yml --limit 3

if ($Watch) {
    $runId = gh run list --repo $repo --workflow=deploy.yml --limit 1 --json databaseId --jq '.[0].databaseId'
    Write-Host "[部署] 实时跟踪运行 #$runId ..." -ForegroundColor Cyan
    gh run watch $runId --repo $repo
}

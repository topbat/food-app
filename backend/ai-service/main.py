# -*- coding: utf-8 -*-
"""
「食研社」AI 微服务入口（FastAPI，端口 8086，前缀 /api/ai）。

提供三个公开接口（见接口契约第 6 节）：
- GET  /api/ai/health        健康检查
- POST /api/ai/substitute    智能食材替换建议（内置规则引擎）
- POST /api/ai/parse-recipe  菜谱自由文本结构化解析（五步法启发式）

统一响应体：{"code":0,"message":"操作成功","data":...}
异常统一返回：{"code":50000,"message":"..."}（不向外泄露堆栈，安全要求）
"""

import time

import uvicorn
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from config import settings, setup_logging
from services.substitute import get_substitute_suggestions
from services.parser import parse_free_text

# 初始化日志（控制台 + logs/ai-service.log 滚动文件）
logger = setup_logging()

app = FastAPI(title="食研社 AI 服务", version="1.0.0", docs_url="/api/ai/docs", openapi_url="/api/ai/openapi.json")

# ------------------------------------------------------------
# CORS：仅放行配置中的来源（安全要求，按环境区分）
# ------------------------------------------------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ------------------------------------------------------------
# 统一响应工具
# ------------------------------------------------------------
def ok(data=None, message: str = "操作成功") -> dict:
    """构造统一成功响应体：{"code":0,"message":"操作成功","data":...}。"""
    return {"code": 0, "message": message, "data": data}


def fail(code: int, message: str) -> JSONResponse:
    """构造统一失败响应体：{"code":<错误码>,"message":"...","data":null}。"""
    return JSONResponse(status_code=200, content={"code": code, "message": message, "data": None})


# ------------------------------------------------------------
# 访问日志中间件：记录每个请求的方法/路径/状态码/耗时
# ------------------------------------------------------------
@app.middleware("http")
async def access_log_middleware(request: Request, call_next):
    """FastAPI 访问日志中间件：记录请求方法、路径、响应状态码与耗时（毫秒）。"""
    start = time.perf_counter()
    response = await call_next(request)
    cost_ms = (time.perf_counter() - start) * 1000
    logger.info("访问日志：%s %s -> %s 耗时=%.2fms",
                request.method, request.url.path, response.status_code, cost_ms)
    return response


# ------------------------------------------------------------
# 全局异常处理：统一返回 50000 / 40000，不泄露堆栈（安全要求）
# ------------------------------------------------------------
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """参数校验异常处理器：统一返回 code=40000，不暴露内部校验细节结构。"""
    logger.warning("参数校验失败：%s %s，原因：%s", request.method, request.url.path, exc.errors())
    return fail(40000, "参数错误，请检查请求体格式")


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """全局异常处理器：堆栈仅写入日志，对外统一返回 code=50000 的安全提示。"""
    logger.exception("服务异常：%s %s，异常信息：%s", request.method, request.url.path, exc)
    return fail(50000, "服务繁忙，请稍后重试")


# ------------------------------------------------------------
# 请求模型（Pydantic）
# ------------------------------------------------------------
class IngredientItem(BaseModel):
    """替换请求中的单个食材：名称 + 用量（克）。"""
    name: str = Field(..., description="食材名称，如：五花肉")
    amount: float = Field(0, description="用量（克），可选")


class SubstituteRequest(BaseModel):
    """智能替换请求体：食材列表 + 健康场景。"""
    ingredients: list[IngredientItem] = Field(default_factory=list, description="食材列表")
    scene: str = Field(..., description="健康场景：减脂/控糖/素食/低嘌呤")


class ParseRecipeRequest(BaseModel):
    """自由文本解析请求体：一段菜谱自由文本。"""
    freeText: str = Field("", description="菜谱自由文本")


# ------------------------------------------------------------
# 接口实现（接口契约第 6 节）
# ------------------------------------------------------------
@app.get("/api/ai/health")
async def health():
    """健康检查接口：返回服务状态与当前环境。"""
    logger.debug("健康检查被调用，当前环境：%s", settings.env)
    return ok({"status": "UP", "env": settings.env})


@app.post("/api/ai/substitute")
async def substitute(req: SubstituteRequest):
    """
    智能食材替换接口：按场景对入参食材逐个匹配内置规则字典，
    返回 suggestions（sourceName/targetName/reason/calorieSavedPer100g），
    未命中时返回空列表与提示文案。
    """
    logger.info("收到替换请求：场景=%s，食材数=%s", req.scene, len(req.ingredients))
    ingredients = [{"name": i.name, "amount": i.amount} for i in req.ingredients]
    result = get_substitute_suggestions(ingredients, req.scene)
    # tip 非空说明无建议，用 message 透出提示文案
    message = result.pop("tip") or "操作成功"
    return ok(result, message=message)


@app.post("/api/ai/parse-recipe")
async def parse_recipe(req: ParseRecipeRequest):
    """
    菜谱自由文本结构化接口：启发式把自由文本拆解为五步法菜谱 JSON Schema
    （recipe_name / steps 五阶段 / timer_minutes / fire_control 等）。
    空文本返回 code=40000 参数错误。
    """
    text = (req.freeText or "").strip()
    if not text:
        # 关键判断分支：空文本直接判定参数错误
        logger.info("解析请求被拒绝：freeText 为空")
        return fail(40000, "freeText 不能为空，请提供菜谱自由文本")
    logger.info("收到解析请求：文本长度=%s", len(text))
    return ok(parse_free_text(text))


# ------------------------------------------------------------
# 启动入口
# ------------------------------------------------------------
if __name__ == "__main__":
    # 启动日志：输出当前环境与端口（安全/运维要求）
    logger.info("AI 服务启动：环境=%s，端口=%s，热重载=%s，CORS放行来源=%s",
                settings.env, settings.PORT, settings.reload, settings.cors_origins)
    uvicorn.run("main:app", host=settings.HOST, port=settings.PORT, reload=settings.reload)

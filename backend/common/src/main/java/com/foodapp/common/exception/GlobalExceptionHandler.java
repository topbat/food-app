package com.foodapp.common.exception;

import com.foodapp.common.result.Result;
import com.foodapp.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

/**
 * 全局异常处理器。
 * 统一拦截各微服务抛出的异常并转换为 Result 结构，
 * 同时输出告警/错误日志，保证异常不以堆栈形式暴露给前端（安全要求）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常：属于预期内异常，记录 warn 级别日志。
     *
     * @param e 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("[全局异常] 业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理 @RequestBody 参数校验失败：提取第一个字段错误返回给前端。
     *
     * @param e 校验异常
     * @return 统一失败响应（含具体字段提示）
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null
                ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                : ResultCode.PARAM_ERROR.getMessage();
        log.warn("[全局异常] 参数校验失败: {}", msg);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理 URL 参数 / 路径参数校验失败。
     *
     * @param e 约束违反异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("[全局异常] 参数约束校验失败: {}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 兜底处理未知异常：记录 error 级别日志（含堆栈），对外只返回笼统提示，避免泄露内部实现。
     *
     * @param e 任意未捕获异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("[全局异常] 未知异常: {}", e.getMessage(), e);
        return Result.fail(ResultCode.SERVER_ERROR);
    }
}

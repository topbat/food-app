package com.foodapp.common.exception;

import com.foodapp.common.result.ResultCode;

/**
 * 业务异常。
 * 业务层主动抛出，由全局异常处理器统一转换为 Result 响应，避免堆栈直接暴露给前端。
 */
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final int code;

    /**
     * 使用预定义错误码构造业务异常。
     *
     * @param resultCode 错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用预定义错误码 + 自定义提示构造业务异常。
     *
     * @param resultCode 错误码枚举
     * @param message    自定义中文提示
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public int getCode() { return code; }
}

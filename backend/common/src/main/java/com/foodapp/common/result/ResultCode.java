package com.foodapp.common.result;

/**
 * 统一业务状态码枚举。
 * 0 表示成功；4xxxx 客户端错误；5xxxx 服务端错误。
 */
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(0, "操作成功"),
    /** 参数校验失败 */
    PARAM_ERROR(40000, "参数校验失败"),
    /** 未登录或登录已过期 */
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    /** 无权限操作 */
    FORBIDDEN(40300, "无权限操作"),
    /** 资源不存在 */
    NOT_FOUND(40400, "资源不存在"),
    /** 业务规则冲突（如重复注册、状态机非法跳转） */
    BIZ_CONFLICT(40900, "业务规则冲突"),
    /** 服务内部错误 */
    SERVER_ERROR(50000, "服务内部错误，请稍后重试"),
    /** 依赖的下游服务调用失败 */
    REMOTE_ERROR(50200, "依赖服务调用失败，请稍后重试");

    /** 状态码 */
    private final int code;
    /** 默认中文提示 */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}

package com.foodapp.common.result;

import java.time.LocalDateTime;

/**
 * 统一响应体。
 * 所有微服务接口必须返回该结构，保证前端处理逻辑一致。
 *
 * @param <T> 业务数据类型
 */
public class Result<T> {

    /** 业务状态码：0 成功，非 0 失败（见 ResultCode） */
    private int code;
    /** 提示信息（中文，可直接展示给用户） */
    private String message;
    /** 业务数据 */
    private T data;
    /** 服务端响应时间 */
    private LocalDateTime timestamp;

    public Result() {
        this.timestamp = LocalDateTime.now();
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造成功响应（无数据）。
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), "操作成功", null);
    }

    /**
     * 构造成功响应（携带数据）。
     *
     * @param data 业务数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), "操作成功", data);
    }

    /**
     * 构造成功响应（自定义提示信息）。
     *
     * @param message 中文提示信息
     * @param data    业务数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 构造失败响应。
     *
     * @param code    错误码
     * @param message 中文错误提示
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 构造失败响应（使用预定义错误码枚举）。
     *
     * @param resultCode 错误码枚举
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

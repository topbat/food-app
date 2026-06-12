package com.foodapp.file.config;

import com.foodapp.common.result.Result;
import com.foodapp.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 上传专用异常处理：multipart 请求体超出 Spring 限制（max-file-size/max-request-size）时，
 * 默认会被 common 的兜底处理器转成 50000，这里提前拦截转为 40000 参数错误 + 友好提示。
 * 注意 @Order(0) 保证优先于 common 的 GlobalExceptionHandler（默认最低优先级）。
 */
@RestControllerAdvice
@Order(0)
public class UploadExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(UploadExceptionAdvice.class);

    /**
     * 处理上传体超限异常。
     *
     * @param e 超限异常
     * @return 40000 统一失败响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("[上传] 拒绝：请求体超出大小限制: {}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), "上传文件过大：图片不超过 10MB，视频不超过 100MB");
    }
}

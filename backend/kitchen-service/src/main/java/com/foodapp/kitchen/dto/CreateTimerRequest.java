package com.foodapp.kitchen.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 开启计时器请求体。
 */
public class CreateTimerRequest {

    /** 计时器名称（如：焯水、焖煮） */
    @NotBlank(message = "计时器名称不能为空")
    @Size(max = 50, message = "计时器名称最长50个字符")
    private String timerName;

    /** 总计时秒数（1秒 ~ 24小时） */
    @NotNull(message = "计时秒数不能为空")
    @Min(value = 1, message = "计时秒数至少为1秒")
    @Max(value = 86400, message = "计时秒数最长为24小时")
    private Integer totalSec;

    public String getTimerName() { return timerName; }
    public void setTimerName(String timerName) { this.timerName = timerName; }
    public Integer getTotalSec() { return totalSec; }
    public void setTotalSec(Integer totalSec) { this.totalSec = totalSec; }
}

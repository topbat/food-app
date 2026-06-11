package com.foodapp.kitchen.dto;

/**
 * 计时器视图对象。
 * 契约：TimerVO = {id,timerName,totalSec,remainSec,status}。
 */
public class TimerVO {

    /** 计时器ID */
    private Long id;
    /** 计时器名称（如：焯水、焖煮） */
    private String timerName;
    /** 总计时秒数 */
    private Integer totalSec;
    /** 剩余秒数（实时计算，最小为0） */
    private Long remainSec;
    /** 计时状态（1计时中 2已完成 3已取消） */
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTimerName() { return timerName; }
    public void setTimerName(String timerName) { this.timerName = timerName; }
    public Integer getTotalSec() { return totalSec; }
    public void setTotalSec(Integer totalSec) { this.totalSec = totalSec; }
    public Long getRemainSec() { return remainSec; }
    public void setRemainSec(Long remainSec) { this.remainSec = remainSec; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

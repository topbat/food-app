package com.foodapp.kitchen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 语音指令解析日志实体。
 * 与 sql/03_kitchen.sql 中 kitchen_voice_log 表结构保持一致。
 */
@Entity
@Table(name = "kitchen_voice_log", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id")
})
@Comment("语音指令解析日志表")
public class KitchenVoiceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "session_id", nullable = false)
    @Comment("关联烹饪会话ID")
    private Long sessionId;

    @Column(name = "command_text", nullable = false, length = 200)
    @Comment("用户语音指令原文（如：下一步）")
    private String commandText;

    @Column(name = "parsed_action", nullable = false, length = 50)
    @Comment("解析出的动作（NEXT_STEP/PREV_STEP/QUERY_TIMER/START_TIMER/UNKNOWN）")
    private String parsedAction;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前填充创建时间。
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getCommandText() { return commandText; }
    public void setCommandText(String commandText) { this.commandText = commandText; }
    public String getParsedAction() { return parsedAction; }
    public void setParsedAction(String parsedAction) { this.parsedAction = parsedAction; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

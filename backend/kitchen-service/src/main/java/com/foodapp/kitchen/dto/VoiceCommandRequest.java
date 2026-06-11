package com.foodapp.kitchen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 语音指令请求体。
 */
public class VoiceCommandRequest {

    /** 用户语音指令原文（如：下一步） */
    @NotBlank(message = "指令文本不能为空")
    @Size(max = 200, message = "指令文本最长200个字符")
    private String commandText;

    public String getCommandText() { return commandText; }
    public void setCommandText(String commandText) { this.commandText = commandText; }
}

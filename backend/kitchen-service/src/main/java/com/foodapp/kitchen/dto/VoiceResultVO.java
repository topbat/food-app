package com.foodapp.kitchen.dto;

/**
 * 语音指令解析结果视图对象。
 * 契约：{parsedAction,message,session:SessionVO?}。
 */
public class VoiceResultVO {

    /** 解析出的动作（NEXT_STEP/PREV_STEP/QUERY_TIMER/START_TIMER/UNKNOWN） */
    private String parsedAction;
    /** 给用户的中文反馈文案 */
    private String message;
    /** 最新会话状态（部分指令返回，便于前端即时刷新） */
    private SessionVO session;

    public String getParsedAction() { return parsedAction; }
    public void setParsedAction(String parsedAction) { this.parsedAction = parsedAction; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public SessionVO getSession() { return session; }
    public void setSession(SessionVO session) { this.session = session; }
}

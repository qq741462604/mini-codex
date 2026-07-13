package com.minicodex.ai;


import lombok.Data;


@Data
public class AiResponse {


    /**
     * 普通文本回复
     */
    private String content;



    /**
     * 工具调用
     */
    private ToolCall toolCall;



}
package com.minicodex.ai;

import com.minicodex.ai.ToolCall;
import lombok.Data;

@Data
public class AiMessage {

    private String role;

    private String content;

    /**
     * assistant调用Tool时
     */
    private ToolCall toolCall;

    /**
     * tool返回
     */
    private String toolName;

}
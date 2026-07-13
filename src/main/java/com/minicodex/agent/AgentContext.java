package com.minicodex.agent;


import com.minicodex.ai.AiMessage;
import lombok.Data;


import java.util.ArrayList;
import java.util.List;



@Data
public class AgentContext {


    /**
     * 用户原始输入
     */
    private String userInput;



    /**
     * 当前执行步骤
     */
    private int step;



    /**
     * AI上下文消息
     */
    private List<AiMessage> messages =
            new ArrayList<>();



}
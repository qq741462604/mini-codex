package com.minicodex.ai;


import lombok.Data;


@Data
public class ToolCall {


    /**
     * 是否调用工具
     */
    private boolean callTool;


    /**
     * 工具名称
     */
    private String toolName;


    /**
     * 参数
     */
    private String arguments;


}
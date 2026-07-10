package com.minicodex.ai;


import lombok.Data;


@Data
public class AiResponse {


    private String content;


    private boolean needTool;


    private String toolName;


    private String toolArguments;


}
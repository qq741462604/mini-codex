package com.minicodex.tool;


import lombok.extern.slf4j.Slf4j;



@Slf4j
public abstract class BaseTool
        implements AgentTool {



    protected void logExecute(
            Object input
    ){

        log.info(
                "execute tool={}, input={}",
                name(),
                summarizeInput(input)
        );

    }


    private Object summarizeInput(
            Object input
    ){


        if(!(input instanceof ToolInput)){

            return input;

        }


        ToolInput toolInput =
                (ToolInput)input;


        return "ToolInput("
                +
                "path="
                +
                toolInput.getPath()
                +
                ", keyword="
                +
                toolInput.getKeyword()
                +
                ", limit="
                +
                toolInput.getLimit()
                +
                ", startLine="
                +
                toolInput.getStartLine()
                +
                ", endLine="
                +
                toolInput.getEndLine()
                +
                ", contentLength="
                +
                length(toolInput.getContent())
                +
                ", oldTextLength="
                +
                length(toolInput.getOldText())
                +
                ", newTextLength="
                +
                length(toolInput.getNewText())
                +
                ")";

    }


    private int length(
            String text
    ){


        return text==null
                ? 0
                : text.length();

    }



}

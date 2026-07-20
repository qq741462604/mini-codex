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
                input
        );

    }



}
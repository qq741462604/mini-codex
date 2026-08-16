package com.minicodex.application.execution;


import lombok.Builder;
import lombok.Data;
import com.minicodex.application.agent.Agent;



@Data
@Builder
/**
 * ToolCallResult：负责 Agent 执行流程中的协调与结果汇总。
 * 所属层：应用层。
 *
 * @author yy
 */
public class ToolCallResult {


    private String tool;


    private boolean success;


    private Object result;


    private String error;




    public static ToolCallResult success(
            String tool,
            Object result
    ){

        return builder()
                .tool(tool)
                .success(true)
                .result(result)
                .build();

    }




    public static ToolCallResult failed(
            String tool,
            String error
    ){

        return builder()
                .tool(tool)
                .success(false)
                .error(error)
                .build();

    }


}
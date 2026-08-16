package com.minicodex.domain.agent;


import com.minicodex.domain.agent.CodeChange;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import com.minicodex.application.agent.Agent;


@Data
@Builder
/**
 * AgentResult：承载 Agent 运行状态、观察结果与领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public class AgentResult {


    private boolean success;


    private String message;


    private Object data;

    private List<CodeChange> changes;
    public static AgentResult success(String message){

        return AgentResult.builder()
                .success(true)
                .message(message)
                .build();
    }


    public static AgentResult failed(String message){

        return AgentResult.builder()
                .success(false)
                .message(message)
                .build();
    }

}
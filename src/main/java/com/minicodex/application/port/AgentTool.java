package com.minicodex.application.port;


import com.minicodex.domain.agent.AgentContext;
import com.minicodex.application.agent.Agent;



/**
 * AgentTool：定义应用层调用外部能力的抽象边界。
 * 所属层：应用层。
 *
 * @author yy
 */
public interface AgentTool {



    /**
     * 工具名称
     */
    String name();



    /**
     * 工具描述
     */
    String description();



    /**
     * 执行工具
     */
    Object execute(
            Object input,
            AgentContext context
    ) throws Exception;

    default void validate(
            Object input
    ){

    }

}
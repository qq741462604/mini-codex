package com.minicodex.domain.agent;


import lombok.Builder;
import lombok.Data;
import com.minicodex.application.agent.Agent;



@Data
@Builder
/**
 * Observation：承载 Agent 运行状态、观察结果与领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public class Observation {


    /**
     * 工具名称
     */
    private String tool;



    /**
     * 是否成功
     */
    private boolean success;



    /**
     * 工具输出
     */
    private Object result;



    /**
     * 错误信息
     */
    private String error;

    private Object input;
}
package com.minicodex.domain.trace;


import lombok.Builder;
import lombok.Data;
import com.minicodex.application.agent.Agent;



@Data
@Builder
/**
 * TraceStep：承载 Agent 执行跟踪领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public class TraceStep {


    /**
     * 类型
     *
     * PLAN
     * TOOL
     * RESULT
     */
    private String type;



    /**
     * 名称
     */
    private String name;



    /**
     * 输入
     */
    private Object input;



    /**
     * 输出
     */
    private Object output;



    /**
     * 耗时
     */
    private long cost;



}
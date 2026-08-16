package com.minicodex.domain.trace;


import lombok.Data;


import java.util.ArrayList;
import java.util.List;
import com.minicodex.application.agent.Agent;



@Data
/**
 * AgentTrace：承载 Agent 执行跟踪领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public class AgentTrace {


    private String task;



    private long startTime;



    private long endTime;



    private List<TraceStep> steps =
            new ArrayList<>();




    public void add(
            TraceStep step
    ){

        steps.add(step);

    }



}
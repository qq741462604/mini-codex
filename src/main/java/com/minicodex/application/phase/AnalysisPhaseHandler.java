package com.minicodex.application.phase;


import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.AgentPhase;
import org.springframework.stereotype.Component;
import com.minicodex.application.agent.Agent;



@Component
/**
 * AnalysisPhaseHandler：负责 Agent 阶段规则和阶段流转。
 * 所属层：应用层。
 *
 * @author yy
 */
public class AnalysisPhaseHandler
        implements AgentPhaseHandler {



    @Override
    public AgentPhase phase(){

        return AgentPhase.ANALYSIS;

    }



    @Override
    public AgentPhase next(
            AgentContext context
    ){


        /*
         *
         * 判断是否已经收集足够信息
         *
         */


        if(context.getObservations()==null){

            return AgentPhase.ANALYSIS;

        }



        boolean hasSearch =
                context.getObservations()
                        .stream()
                        .anyMatch(
                                o ->
                                        "search_code"
                                                .equals(
                                                        o.getTool()
                                                )
                        );


        if(hasSearch){

            return AgentPhase.CODING;

        }



        return AgentPhase.ANALYSIS;

    }

}
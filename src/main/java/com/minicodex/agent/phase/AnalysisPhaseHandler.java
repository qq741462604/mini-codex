package com.minicodex.agent.phase;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;
import org.springframework.stereotype.Component;



@Component
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
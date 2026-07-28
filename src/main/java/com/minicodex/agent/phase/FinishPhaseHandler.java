package com.minicodex.agent.phase;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;
import org.springframework.stereotype.Component;


@Component
public class FinishPhaseHandler
        implements AgentPhaseHandler {



    @Override
    public AgentPhase phase(){

        return AgentPhase.FINISH;

    }



    @Override
    public AgentPhase next(
            AgentContext context
    ){

        return AgentPhase.FINISH;

    }


}
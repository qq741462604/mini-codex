package com.minicodex.agent.phase;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;
import org.springframework.stereotype.Component;



@Component
public class VerifyPhaseHandler
        implements AgentPhaseHandler {



    @Override
    public AgentPhase phase(){

        return AgentPhase.VERIFY;

    }



    @Override
    public AgentPhase next(
            AgentContext context
    ){


        /*
         *
         * 后面接 Validator
         *
         */


        return AgentPhase.FINISH;

    }


}
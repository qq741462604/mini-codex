package com.minicodex.agent.phase;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;
import org.springframework.stereotype.Component;


@Component
public class RepairPhaseHandler
        implements AgentPhaseHandler {


    @Override
    public AgentPhase phase(){

        return AgentPhase.REPAIR;

    }



    @Override
    public AgentPhase next(
            AgentContext context
    ){

        if(context.getVerifyErrors()
                !=null
                &&
                !context.getVerifyErrors()
                        .isEmpty()){


            return AgentPhase.CODING;

        }


        return AgentPhase.FINISH;

    }

}
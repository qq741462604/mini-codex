package com.minicodex.agent.phase;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;
import org.springframework.stereotype.Component;



@Component
public class CodingPhaseHandler
        implements AgentPhaseHandler {



    @Override
    public AgentPhase phase(){

        return AgentPhase.CODING;

    }



    @Override
    public AgentPhase next(
            AgentContext context
    ){



        boolean changed =
                context.getObservations()
                        .stream()
                        .anyMatch(
                                o ->
                                        (
                                                "create_file".equals(o.getTool())
                                                        ||
                                                        "write_file".equals(o.getTool())
                                                        ||
                                                        "edit_file".equals(o.getTool())
                                        )
                                                &&
                                                o.isSuccess()
                        );



        if(changed){

            return AgentPhase.VERIFY;

        }


        return AgentPhase.CODING;

    }

}
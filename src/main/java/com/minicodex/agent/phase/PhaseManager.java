package com.minicodex.agent.phase;


import com.minicodex.agent.AgentPhase;
import org.springframework.stereotype.Component;

import java.util.List;



@Component
public class PhaseManager {


    private final List<AgentPhaseHandler> handlers;



    public PhaseManager(
            List<AgentPhaseHandler> handlers
    ){

        this.handlers=handlers;

    }



    public AgentPhase next(
            AgentPhase current,
            com.minicodex.agent.AgentContext context
    ){


        return handlers.stream()
                .filter(
                        h ->
                                h.phase()==current
                )
                .findFirst()
                .map(
                        h ->
                                h.next(context)
                )
                .orElse(
                        AgentPhase.FINISH
                );


    }


}
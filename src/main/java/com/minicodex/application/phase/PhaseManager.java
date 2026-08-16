package com.minicodex.application.phase;


import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.AgentPhase;
import org.springframework.stereotype.Component;

import java.util.List;
import com.minicodex.application.agent.Agent;



@Component
/**
 * PhaseManager：负责 Agent 阶段规则和阶段流转。
 * 所属层：应用层。
 *
 * @author yy
 */
public class PhaseManager {


    private final List<AgentPhaseHandler> handlers;



    public PhaseManager(
            List<AgentPhaseHandler> handlers
    ){

        this.handlers=handlers;

    }



    public AgentPhase next(
            AgentPhase current,
            AgentContext context
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
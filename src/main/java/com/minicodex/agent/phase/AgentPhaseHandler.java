package com.minicodex.agent.phase;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;


public interface AgentPhaseHandler {


    AgentPhase phase();


    AgentPhase next(
            AgentContext context
    );


}
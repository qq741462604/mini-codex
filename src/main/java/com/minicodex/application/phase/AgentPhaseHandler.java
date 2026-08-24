package com.minicodex.application.phase;

import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.AgentPhase;

/**
 * AgentPhaseHandler：负责 Agent 阶段规则和阶段流转。 所属层：应用层。
 *
 * @author yy
 */
public interface AgentPhaseHandler {

  AgentPhase phase();

  AgentPhase next(AgentContext context);
}

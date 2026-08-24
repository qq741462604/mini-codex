package com.minicodex.application.planning;

import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.plan.CodePlan;

/**
 * Planner：负责生成和校验代码改造计划。 所属层：应用层。
 *
 * @author yy
 */
public interface Planner {

  CodePlan createPlan(AgentContext context);
}

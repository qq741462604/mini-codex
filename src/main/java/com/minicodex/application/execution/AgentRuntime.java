package com.minicodex.application.execution;

import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.AgentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * AgentRuntime：负责 Agent 执行流程中的协调与结果汇总。 所属层：应用层。
 *
 * @author yy
 */
public class AgentRuntime {

  private final AgentExecutor executor;

  public AgentResult execute(AgentContext context) {

    try {

      return executor.execute(context);

    } catch (Exception e) {

      log.error("runtime execute error", e);

      return AgentResult.failed(e.getClass().getName() + ":" + e.getMessage());
    }
  }
}

package com.minicodex.bootstrap;

import com.minicodex.application.agent.Agent;
import com.minicodex.application.agent.AgentContextFactory;
import com.minicodex.application.execution.AgentRuntime;
import com.minicodex.application.memory.AgentMemoryService;
import com.minicodex.application.service.TraceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * AgentConfig：负责 Spring Boot 启动与依赖装配。 所属层：启动装配。
 *
 * @author yy
 */
public class AgentConfig {

  @Bean
  public Agent codingAgent(
      AgentRuntime runtime,
      TraceService traceService,
      AgentContextFactory agentContextFactory,
      AgentMemoryService memoryService) {

    return Agent.builder()
        .id("default-agent")
        .name("Mini-Codex-Agent")
        .runtime(runtime)
        .traceService(traceService)
        .agentContextFactory(agentContextFactory)
        .memoryService(memoryService)
        .build();
  }
}

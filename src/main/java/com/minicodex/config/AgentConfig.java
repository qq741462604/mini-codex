package com.minicodex.config;


import com.minicodex.agent.Agent;
import com.minicodex.agent.AgentContextFactory;
import com.minicodex.runtime.AgentRuntime;
import com.minicodex.trace.TraceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class AgentConfig {


    @Bean
    public Agent codingAgent(
            AgentRuntime runtime,
            TraceService traceService,
            AgentContextFactory agentContextFactory
    ){


        return Agent.builder()
                .id("default-agent")
                .name("Mini-Codex-Agent")
                .runtime(runtime)
                .traceService(traceService)
                .agentContextFactory(agentContextFactory)
                .build();


    }


}

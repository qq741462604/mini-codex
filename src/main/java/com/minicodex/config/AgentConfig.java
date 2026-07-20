package com.minicodex.config;


import com.minicodex.agent.Agent;
import com.minicodex.memory.MemoryStore;
import com.minicodex.runtime.AgentRuntime;
import com.minicodex.skill.SkillLoader;
import com.minicodex.trace.TraceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class AgentConfig {


    @Bean
    public Agent codingAgent(
            AgentRuntime runtime,
            MemoryStore memoryStore,
            TraceService traceService,
            SkillLoader skillLoader
    ){


        return Agent.builder()
                .id("default-agent")
                .name("Mini-Codex-Agent")
                .runtime(runtime)
                .memoryStore(memoryStore)
                .traceService(traceService)
                .skillLoader(skillLoader)
                .build();


    }


}
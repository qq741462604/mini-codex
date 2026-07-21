package com.minicodex.agent;


import com.minicodex.memory.Memory;
import com.minicodex.memory.MemoryStore;
import com.minicodex.runtime.AgentRuntime;
import com.minicodex.skill.SkillLoader;
import com.minicodex.trace.AgentTrace;
import com.minicodex.trace.TraceService;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;


@Data
@Builder
@Slf4j
public class Agent {


    private String id;


    private String name;


    private AgentRuntime runtime;


    private MemoryStore memoryStore;

    private TraceService traceService;
    private SkillLoader skillLoader;

    public AgentResult run(
            String task
    ) {
        AgentTrace trace =
                traceService.start(task);
        AgentResult result;
        try {


            AgentContext context =
                    AgentContext.builder()
                            .agentId(id)
                            .task(task)
                            .trace(trace)
                            .phase(
                                    AgentPhase.ANALYSIS
                            )
                            .memories(
                                    memoryStore == null
                                            ?
                                            new ArrayList<>()
                                            :
                                            memoryStore.query(task)
                            )
                            .observations(
                                    new ArrayList<>()
                            )
                            .skills(
                                    skillLoader == null
                                            ?
                                            new ArrayList<>()
                                            :
                                            skillLoader.load()
                            )
                            .build();

            log.info(
                    "agent trace = {}",
                    context.getTrace()
            );

            result =
                    runtime.execute(
                            context
                    );

            memoryStore.save(
                    Memory.builder()
                            .key(task)
                            .content(
                                    result.getMessage()
                            )
                            .build()
            );

        } catch (Exception e) {
            log.error(
                    "agent run failed",
                    e
            );
            result =
                    AgentResult.failed(
                            e.getMessage()
                    );

        } finally {


            traceService.finish(trace);

        }


        return result;


    }


}
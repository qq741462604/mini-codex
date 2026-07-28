package com.minicodex.agent;


import com.minicodex.memory.AgentMemoryService;
import com.minicodex.memory.Memory;
import com.minicodex.memory.MemoryStore;
import com.minicodex.memory.MemoryType;
import com.minicodex.runtime.AgentRuntime;
import com.minicodex.skill.SkillLoader;
import com.minicodex.trace.AgentTrace;
import com.minicodex.trace.TraceService;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@Builder
@Slf4j
public class Agent {


    private String id;


    private String name;


    private AgentRuntime runtime;


    private MemoryStore memoryStore;
    private AgentMemoryService memoryService;
    private TraceService traceService;
    private SkillLoader skillLoader;

    public AgentResult run(
            String task
    ) {
        AgentTrace trace = null;

        if(traceService!=null){

            trace =
                    traceService.start(task);

        }
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
                                            safeMemoryQuery(task)
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

            if(runtime==null){

                return AgentResult.failed(
                        "AgentRuntime is null"
                );

            }
            result =
                    runtime.execute(
                            context
                    );


            if(memoryService!=null
                    &&
                    result!=null){

                memoryService.saveTask(
                        task,
                        result.getMessage()
                );

            }


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


            if(traceService!=null
                    &&
                    trace!=null){

                traceService.finish(trace);

            }

        }


        return result;


    }

    private List<Memory> safeMemoryQuery(
            String task
    ){

        List<Memory> memories =
                memoryStore.query(task);


        return memories==null
                ?
                new ArrayList<>()
                :
                memories;

    }
}
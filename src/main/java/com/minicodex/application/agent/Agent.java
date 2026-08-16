package com.minicodex.application.agent;

import com.minicodex.application.memory.AgentMemoryService;
import com.minicodex.application.execution.AgentRuntime;
import com.minicodex.domain.trace.AgentTrace;
import com.minicodex.application.service.TraceService;
import lombok.Builder;
import lombok.Data;
import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.AgentResult;
import com.minicodex.domain.memory.Memory;

@Data
@Builder
/**
 * Agent：负责 Agent 用例的创建与批量编排。
 * 所属层：应用层。
 *
 * @author yy
 */
public class Agent {

    private String id;
    private String name;
    private AgentRuntime runtime;
    private AgentMemoryService memoryService;
    private TraceService traceService;
    private AgentContextFactory agentContextFactory;

    public AgentResult run(String task) {
        AgentTrace trace = null;
        if (traceService != null) {
            trace = traceService.start(task);
        }
        AgentResult result;
        try {
            if (agentContextFactory == null) {
                return AgentResult.failed("AgentContextFactory is null");
            }
            AgentContext context = agentContextFactory.create(id, task, trace);
            if (runtime == null) {
                return AgentResult.failed("AgentRuntime is null");
            }
            result = runtime.execute(context);
            if (memoryService != null && result != null) {
                memoryService.saveTask(task, result.getMessage());
            }
        } catch (Exception e) {
            result = AgentResult.failed(e.getMessage());
        } finally {
            if (traceService != null && trace != null) {
                traceService.finish(trace);
            }
        }
        return result;
    }
}

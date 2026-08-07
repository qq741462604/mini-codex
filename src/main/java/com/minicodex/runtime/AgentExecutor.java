package com.minicodex.runtime;

import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentResult;
import com.minicodex.agent.result.CodeChangeExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentExecutor {

    private final AgentLoop agentLoop;
    private final CodeChangeExtractor extractor;

    public AgentResult execute(AgentContext context) {
        try {
            agentLoop.run(context);
            return AgentResult.builder()
                    .success(true)
                    .message("agent execute success")
                    .data(context.getObservations())
                    .changes(extractor.extract(context.getObservations()))
                    .build();
        } catch (Exception e) {
            return AgentResult.failed(e.toString());
        }
    }
}

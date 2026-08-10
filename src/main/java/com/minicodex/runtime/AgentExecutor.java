package com.minicodex.runtime;

import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentResult;
import com.minicodex.agent.observation.Observation;
import com.minicodex.agent.result.CodeChange;
import com.minicodex.agent.result.CodeChangeExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AgentExecutor {

    private final AgentLoop agentLoop;
    private final CodeChangeExtractor extractor;

    public AgentResult execute(AgentContext context) {
        try {
            agentLoop.run(context);
            List<CodeChange> changes = extractor.extract(context.getObservations());
            String failure = findFailure(context);
            if (failure != null) {
                return AgentResult.builder()
                        .success(false)
                        .message(failure)
                        .data(context.getObservations())
                        .changes(changes)
                        .build();
            }
            return AgentResult.builder()
                    .success(true)
                    .message("agent execute success")
                    .data(context.getObservations())
                    .changes(changes)
                    .build();
        } catch (Exception e) {
            return AgentResult.failed(e.toString());
        }
    }

    private String findFailure(AgentContext context) {
        if (context.getVerifyErrors() != null && !context.getVerifyErrors().isEmpty()) {
            return "verify failed: " + String.join("; ", context.getVerifyErrors());
        }

        if (context.getLastObservations() == null || context.getLastObservations().isEmpty()) {
            return null;
        }

        for (Observation observation : context.getLastObservations()) {
            if (!observation.isSuccess()) {
                return "tool failed: "
                        + observation.getTool()
                        + " "
                        + observation.getError();
            }
        }

        return null;
    }
}

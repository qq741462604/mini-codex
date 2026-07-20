package com.minicodex.runtime;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentResult;
import com.minicodex.agent.result.CodeChangeExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class AgentExecutor {


    private final AgentLoop agentLoop;
    private final CodeChangeExtractor extractor;

    public AgentResult execute(
            AgentContext context
    ) {


        try {


            log.info(
                    "===== AgentExecutor START ====="
            );


            agentLoop.run(context);


            log.info(
                    "===== AgentExecutor END ====="
            );
            log.info(
                    "observations={}",
                    context.getObservations()
            );

            return AgentResult.builder()
                    .success(true)
                    .message(
                            "agent execute success"
                    )
                    .data(
                            context.getObservations()
                    )
                    .changes(
                            extractor.extract(
                                    context.getObservations()
                            )
                    )
                    .build();


        } catch (Exception e) {


            log.error(
                    "agent execute failed",
                    e
            );


            return AgentResult.failed(
                    e.toString()
            );

        }


    }


}
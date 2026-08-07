package com.minicodex.runtime;

import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;
import com.minicodex.agent.AgentStatus;
import com.minicodex.agent.phase.PhaseManager;
import com.minicodex.planner.CodePlan;
import com.minicodex.planner.Planner;
import com.minicodex.project.ProjectIndex;
import com.minicodex.project.ProjectIndexer;
import com.minicodex.verify.VerifyEngine;
import com.minicodex.verify.VerifyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentLoop {

    private static final int MAX_ITERATION = 10;

    private final Planner planner;
    private final ToolExecutor toolExecutor;
    private final ProjectIndexer projectIndexer;
    private final PhaseManager phaseManager;
    private final VerifyEngine verifyEngine;

    public void run(AgentContext context) {
        log.info("agent loop start task={}", context.getTask());
        context.setStatus(AgentStatus.ANALYZING);

        Set<String> executedPlans = new HashSet<>();
        Map<String, Integer> failureCounts = new HashMap<>();

        for (int i = 0; i < MAX_ITERATION; i++) {
            if (context.getPhase() == AgentPhase.FINISH) {
                return;
            }
            if (context.getPhase() == AgentPhase.VERIFY) {
                if (handleVerifyPhase(context)) {
                    return;
                }
                continue;
            }

            CodePlan plan = planner.createPlan(context);
            if (isEmptyPlan(plan)) {
                handleEmptyPlan(context);
                if (context.getPhase() == AgentPhase.FINISH) {
                    return;
                }
                continue;
            }
            String planKey = plan.toString();
            if (executedPlans.contains(planKey)) {
                log.warn("duplicate plan detected {}", planKey);
                return;
            }
            executedPlans.add(planKey);

            List<ToolCallResult> results = toolExecutor.execute(plan, context);
            if (shouldStopRepeatedFailure(results, failureCounts)) {
                context.setPhase(AgentPhase.FINISH);
                log.warn("stop repeated tool failures");
                return;
            }

            ProjectIndex index = projectIndexer.build(context.getObservations());
            context.setProjectIndex(index);
            AgentPhase current = context.getPhase();
            AgentPhase next = phaseManager.next(current, context);
            log.info("phase change {} -> {}", current, next);
            context.setPhase(next);
        }
        log.warn("agent reach max iteration");
    }

    private boolean handleVerifyPhase(AgentContext context) {
        VerifyResult verifyResult = verifyEngine.verify(context);
        if (verifyResult.isSuccess()) {
            context.setPhase(AgentPhase.FINISH);
            return true;
        }
        context.setPhase(AgentPhase.REPAIR);
        context.getVerifyErrors().clear();
        context.getVerifyErrors().addAll(verifyResult.getErrors());
        log.warn("verify failed errors={}", verifyResult.getErrors());
        return false;
    }

    private boolean isEmptyPlan(CodePlan plan) {
        return plan == null || plan.getSteps() == null || plan.getSteps().isEmpty();
    }

    private void handleEmptyPlan(AgentContext context) {
        log.warn("empty plan phase={}", context.getPhase());
        if (context.getPhase() == AgentPhase.CODING) {
            context.setPhase(AgentPhase.FINISH);
            return;
        }
        AgentPhase next = phaseManager.next(context.getPhase(), context);
        context.setPhase(next);
    }

    private boolean shouldStopRepeatedFailure(
            List<ToolCallResult> results,
            Map<String, Integer> failureCounts
    ) {
        String signature = failureSignature(results);
        if (signature == null) {
            return false;
        }
        Integer count = failureCounts.get(signature);
        if (count == null) {
            count = 0;
        }
        count++;
        failureCounts.put(signature, count);
        return count >= 2;
    }

    private String failureSignature(List<ToolCallResult> results) {
        if (results == null || results.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (ToolCallResult result : results) {
            if (result.isSuccess()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append(result.getTool())
                    .append(":")
                    .append(result.getError());
        }
        return sb.length() == 0 ? null : sb.toString();
    }
}

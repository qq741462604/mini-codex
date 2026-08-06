package com.minicodex.runtime;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;
import com.minicodex.agent.AgentStatus;
import com.minicodex.agent.phase.PhaseManager;
import com.minicodex.planner.CodePlan;
import com.minicodex.planner.PlanStep;
import com.minicodex.planner.Planner;
import com.minicodex.project.ProjectIndex;
import com.minicodex.project.ProjectIndexer;
import com.minicodex.tool.ToolInput;
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


    public void run(
            AgentContext context
    ) {


        log.info(
                "===== AgentLoop START task={} =====",
                context.getTask()
        );


        context.setStatus(
                AgentStatus.ANALYZING
        );


        Set<String> executedPlans =
                new HashSet<>();


        Map<String,Integer> failureCounts =
                new HashMap<>();


        for (int i = 0; i < MAX_ITERATION; i++) {


            log.info(
                    "iteration={} phase={}",
                    i + 1,
                    context.getPhase()
            );


            if (context.getPhase()
                    ==
                    AgentPhase.FINISH) {


                log.info(
                        "agent already finish"
                );


                return;

            }



            /*
             *
             * VERIFY阶段单独处理
             *
             */
            if (context.getPhase()
                    ==
                    AgentPhase.VERIFY) {


                VerifyResult verifyResult =
                        verifyEngine.verify(
                                context
                        );


                if (verifyResult.isSuccess()) {


                    context.setPhase(
                            AgentPhase.FINISH
                    );


                    log.info(
                            "verify success finish"
                    );


                    return;


                } else {


                    context.setPhase(
                            AgentPhase.REPAIR
                    );


                    context.getVerifyErrors()
                            .clear();


                    context.getVerifyErrors()
                            .addAll(
                                    verifyResult.getErrors()
                            );


                    log.warn(
                            "verify failed errors={}",
                            verifyResult.getErrors()
                    );


                    continue;

                }

            }


            CodePlan plan = planner.createPlan(context);


            if (plan == null
                    ||
                    plan.getSteps() == null
                    ||
                    plan.getSteps().isEmpty()) {

                log.warn(
                        "empty plan phase={}",
                        context.getPhase()
                );

                if (context.getPhase()
                        == AgentPhase.CODING) {

                    log.error(
                            "CODING phase but planner returned empty plan, stop"
                    );

                    context.setPhase(
                            AgentPhase.FINISH
                    );

                    return;

                }


                AgentPhase next =
                        phaseManager.next(
                                context.getPhase(),
                                context
                        );


                context.setPhase(
                        next
                );


                continue;

            }


            String planKey =
                    plan.toString();


            if (executedPlans.contains(planKey)) {


                log.warn(
                        "duplicate plan detected {}",
                        planKey
                );


                return;

            }


            executedPlans.add(
                    planKey
            );


            log.info(
                    "execute plan summary={}",
                    summarizePlan(plan)
            );


            List<ToolCallResult> results =
                    toolExecutor.execute(
                            plan,
                            context
                    );


            log.info(
                    "tool results summary={}",
                    summarizeToolResults(results)
            );


            if(shouldStopRepeatedFailure(
                    results,
                    failureCounts
            )){

                context.setPhase(
                        AgentPhase.FINISH
                );

                log.warn(
                        "stop repeated tool failures"
                );

                return;

            }


            ProjectIndex index =
                    projectIndexer.build(
                            context.getObservations()
                    );


            context.setProjectIndex(
                    index
            );


            log.info("project index rebuilt");






            /*
             *
             * 根据当前阶段和执行结果推进状态
             *
             */
            AgentPhase current =
                    context.getPhase();


            AgentPhase next =
                    phaseManager.next(
                            current,
                            context
                    );


            log.info(
                    "phase change {} -> {}",
                    current,
                    next
            );


            context.setPhase(
                    next
            );


        }


        log.warn(
                "agent reach max iteration"
        );


    }


    private String summarizeToolResults(
            List<ToolCallResult> results
    ){


        if(results==null
                ||
                results.isEmpty()){

            return "empty";

        }


        StringBuilder sb =
                new StringBuilder();


        for(ToolCallResult result:results){

            if(sb.length()>0){

                sb.append("; ");

            }


            sb.append(result.getTool())
                    .append(":")
                    .append(result.isSuccess() ? "success" : "failed");


            if(!result.isSuccess()
                    &&
                    result.getError()!=null){

                sb.append("(")
                        .append(result.getError())
                        .append(")");

            }

        }


        return sb.toString();

    }


    private String summarizePlan(
            CodePlan plan
    ){


        if(plan==null
                ||
                plan.getSteps()==null
                ||
                plan.getSteps().isEmpty()){

            return "empty";

        }


        StringBuilder sb =
                new StringBuilder();


        for(PlanStep step:plan.getSteps()){

            if(sb.length()>0){

                sb.append("; ");

            }


            sb.append(step.getTool());


            if(step.getInput() instanceof ToolInput){

                ToolInput input =
                        (ToolInput)step.getInput();


                sb.append("(")
                        .append(input.getPath())
                        .append(")");

            }

        }


        return sb.toString();

    }


    private boolean shouldStopRepeatedFailure(
            List<ToolCallResult> results,
            Map<String,Integer> failureCounts
    ){


        String signature =
                failureSignature(results);


        if(signature==null){

            return false;

        }


        Integer count =
                failureCounts.get(signature);


        if(count==null){

            count=0;

        }


        count++;

        failureCounts.put(
                signature,
                count
        );


        return count>=2;

    }


    private String failureSignature(
            List<ToolCallResult> results
    ){


        if(results==null
                ||
                results.isEmpty()){

            return null;

        }


        StringBuilder sb =
                new StringBuilder();


        for(ToolCallResult result:results){

            if(result.isSuccess()){

                continue;

            }


            if(sb.length()>0){

                sb.append("|");

            }


            sb.append(result.getTool())
                    .append(":")
                    .append(result.getError());

        }


        return sb.length()==0
                ? null
                : sb.toString();

    }


}

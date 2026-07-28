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


import java.util.HashSet;
import java.util.List;
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
    ){


        log.info(
                "===== AgentLoop START task={} =====",
                context.getTask()
        );



        context.setStatus(
                AgentStatus.ANALYZING
        );


        Set<String> executedPlans =
                new HashSet<>();



        for(int i=0;i<MAX_ITERATION;i++){


            log.info(
                    "iteration={} phase={}",
                    i + 1,
                    context.getPhase()
            );



            if(context.getPhase()
                    ==
                    AgentPhase.FINISH){


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
            if(context.getPhase()
                    ==
                    AgentPhase.VERIFY){


                VerifyResult verifyResult =
                        verifyEngine.verify(
                                context
                        );


                if(verifyResult.isSuccess()){


                    context.setPhase(
                            AgentPhase.FINISH
                    );


                    log.info(
                            "verify success finish"
                    );


                    return;


                }else{


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




            CodePlan plan;


            try{


                plan =
                        planner.createPlan(
                                context
                        );


            }catch(Exception e){


                log.error(
                        "planner failed",
                        e
                );


                return;

            }





            if(plan==null
                    ||
                    plan.getSteps()==null
                    ||
                    plan.getSteps().isEmpty()){



                log.info(
                        "empty plan"
                );


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



            if(executedPlans.contains(planKey)){


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
                    "execute plan={}",
                    plan
            );




            List<ToolCallResult> results =
                    toolExecutor.execute(
                            plan,
                            context
                    );



            log.info(
                    "tool results={}",
                    results
            );





            ProjectIndex index =
                    projectIndexer.build(
                            context.getObservations()
                    );



            context.setProjectIndex(
                    index
            );



            log.info(
                    "project index={}",
                    index
            );






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


}
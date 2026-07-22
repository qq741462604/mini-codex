package com.minicodex.runtime;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;
import com.minicodex.agent.AgentStatus;
import com.minicodex.agent.observation.Observation;
import com.minicodex.agent.phase.PhaseManager;
import com.minicodex.planner.CodePlan;
import com.minicodex.planner.Planner;
import com.minicodex.project.ProjectIndex;
import com.minicodex.project.ProjectIndexer;
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
                    "current phase={}",
                    context.getPhase()
            );

            if(context.getPhase()
                    ==
                    AgentPhase.FINISH){

                return;

            }
            log.info(
                    "agent iteration={}",
                    i+1
            );





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
                        "empty plan finish"
                );


                return;

            }





            String planKey =
                    plan.toString();



            if(executedPlans.contains(planKey)){


                log.warn(
                        "duplicate plan detected, stop"
                );


                return;

            }



            executedPlans.add(
                    planKey
            );



            log.info(
                    "plan={}",
                    plan
            );





            List<ToolCallResult> results =
                    toolExecutor.execute(
                            plan,
                            context
                    );


            AgentPhase nextPhase =
                    phaseManager.next(
                            context.getPhase(),
                            context
                    );


            log.info(
                    "phase change {} -> {}",
                    context.getPhase(),
                    nextPhase
            );



            context.setPhase(
                    nextPhase
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
             * 核心：
             * 只有真正修改成功才结束
             *
             */
            if(shouldEnterVerifyPhase(results)){

                log.info(
                        "code changed, switch verify"
                );


                context.setPhase(
                        AgentPhase.VERIFY
                );

            }


            log.info(
                    "no code change, continue next iteration"
            );





        }



        log.warn(
                "agent reach max iteration"
        );

    }












    private boolean shouldEnterVerifyPhase(
            List<ToolCallResult> results
    ){



        for(ToolCallResult result:results){


            if(!result.isSuccess()){

                continue;

            }



            String tool =
                    result.getTool();



            if(
                    "create_file".equals(tool)
                            ||
                            "write_file".equals(tool)
                            ||
                            "edit_file".equals(tool)
            ){


                return true;

            }


        }



        return false;

    }


}
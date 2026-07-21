package com.minicodex.runtime;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.AgentPhase;
import com.minicodex.agent.AgentStatus;
import com.minicodex.agent.observation.Observation;
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


    private static final int MAX_ITERATION = 5;


    private final Planner planner;


    private final ToolExecutor toolExecutor;


    private final ProjectIndexer projectIndexer;



    public void run(
            AgentContext context
    ){


        log.info(
                "===== AgentLoop START task={} =====",
                context.getTask()
        );

        if(context.getPhase()==null){

            context.setPhase(
                    AgentPhase.ANALYSIS
            );

        }

        context.setStatus(
                AgentStatus.ANALYZING
        );

        Set<String> executedPlans =
                new HashSet<>();



        for(int i=0;i<MAX_ITERATION;i++){



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


            updatePhase(
                    context,
                    results
            );
            log.info(
                    "===== TOOL RESULTS ===== {}",
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
             * 核心：
             * 只有真正修改成功才结束
             *
             */
            if(hasSuccessfulCodeChange(results)){

                log.info(
                        "code changed successfully, finish agent"
                );

                return;

            }


            log.info(
                    "no code change, continue next iteration"
            );





        }



        log.warn(
                "agent reach max iteration"
        );

    }






    private void updatePhase(
            AgentContext context,
            List<ToolCallResult> results
    ){


        boolean changed=false;


        for(ToolCallResult result:results){


            if(!result.isSuccess()){

                continue;

            }


            String tool =
                    result.getTool();


            if("create_file".equals(tool)
                    ||
                    "write_file".equals(tool)
                    ||
                    "edit_file".equals(tool)
            ){

                changed=true;

            }

        }


        if(changed){


            context.setPhase(
                    AgentPhase.VERIFY
            );


            return;

        }



        if(context.getPhase()
                ==
                AgentPhase.ANALYSIS){


            context.setPhase(
                    AgentPhase.CODING
            );

        }


    }





    private boolean hasSuccessfulCodeChange(
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
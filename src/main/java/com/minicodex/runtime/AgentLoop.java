package com.minicodex.runtime;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.planner.CodePlan;
import com.minicodex.planner.Planner;
import com.minicodex.project.ProjectIndex;
import com.minicodex.project.ProjectIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Component;


import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AgentLoop {


    /**
     * 最大思考轮次
     */
    private static final int MAX_ITERATION = 5;


    /**
     * 最大分析轮次
     *
     * 防止一直 search/read
     */
    private static final int MAX_ANALYSIS_ITERATION = 2;



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



        int analysisCount = 0;



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



            log.info(
                    "plan={}",
                    plan
            );





            /**
             * 判断是不是分析动作
             */
            boolean analysisPlan =
                    isAnalysisPlan(
                            plan
                    );



            if(analysisPlan){


                analysisCount++;


                log.info(
                        "analysis phase count={}",
                        analysisCount
                );


            }





            List<ToolCallResult> results =
                    toolExecutor.execute(
                            plan,
                            context
                    );



            appendObservation(
                    context,
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





            /**
             * 如果已经修改代码
             * 直接结束
             */
            if(hasCodeChange(results)){


                log.info(
                        "code changed, observations={}",
                        context.getObservations()
                );


                return;

            }






            /**
             * 已经分析两轮
             *
             * 强制进入下一阶段
             *
             * 下一次Planner必须生成代码
             */
            if(analysisCount>=MAX_ANALYSIS_ITERATION){


                log.info(
                        "analysis enough, continue coding phase"
                );


            }




        }



        log.warn(
                "agent reach max iteration"
        );



    }









    private void appendObservation(
            AgentContext context,
            List<ToolCallResult> results
    ){


        for(ToolCallResult result:results){


            context.getObservations()
                    .add(
                            Observation.builder()
                                    .tool(
                                            result.getTool()
                                    )
                                    .success(
                                            result.isSuccess()
                                    )
                                    .result(
                                            result.getResult()
                                    )
                                    .error(
                                            result.getError()
                                    )
                                    .build()
                    );


        }


    }









    /**
     * 判断是否进入代码修改阶段
     */
    private boolean hasCodeChange(
            List<ToolCallResult> results
    ){


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
                    "edit_file".equals(tool)){


                return true;

            }


        }


        return false;

    }









    /**
     * 当前Plan是否只是分析
     */
    private boolean isAnalysisPlan(
            CodePlan plan
    ){


        for(var step:plan.getSteps()){


            String tool =
                    step.getTool();



            if("create_file".equals(tool)
                    ||
                    "write_file".equals(tool)
                    ||
                    "edit_file".equals(tool)){


                return false;

            }


        }


        return true;

    }



}
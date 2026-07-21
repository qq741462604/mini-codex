package com.minicodex.runtime;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.planner.CodePlan;
import com.minicodex.planner.PlanStep;
import com.minicodex.tool.AgentTool;
import com.minicodex.tool.FileOperationResult;
import com.minicodex.trace.TraceStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutor {


    private final ToolRegistry toolRegistry;




    public List<ToolCallResult> execute(
            CodePlan plan,
            AgentContext context
    ){


        List<ToolCallResult> results =
                new ArrayList<>();



        for(PlanStep step:plan.getSteps()){



            /*
             *
             * 防止重复create
             *
             */
            if(shouldSkipDuplicateCreate(
                    step,
                    context
            )){

                com.minicodex.tool.ToolInput input =
                        (com.minicodex.tool.ToolInput) step.getInput();



                String path =
                        input.getPath();
                log.info(
                        "skip duplicate create file={}",
                        path
                );


                results.add(
                        ToolCallResult.failed(
                                step.getTool(),
                                "file already created in current agent run"
                        )
                );


                continue;

            }




            AgentTool tool =
                    toolRegistry.getTool(
                            step.getTool()
                    );



            if(tool==null){


                results.add(
                        ToolCallResult.failed(
                                step.getTool(),
                                "tool not found"
                        )
                );


                continue;

            }





            try{


                long start =
                        System.currentTimeMillis();



                tool.validate(
                        step.getInput()
                );



                Object result =
                        tool.execute(
                                step.getInput(),
                                context
                        );



                long cost =
                        System.currentTimeMillis()
                                -
                                start;



                if(context.getTrace()!=null){


                    context.getTrace()
                            .add(
                                    TraceStep.builder()
                                            .type("TOOL")
                                            .name(step.getTool())
                                            .input(step.getInput())
                                            .output(result)
                                            .cost(cost)
                                            .build()
                            );


                }



                ToolCallResult callResult =
                        buildToolResult(
                                step.getTool(),
                                result
                        );



                results.add(
                        callResult
                );

                context.getObservations()
                        .add(
                                Observation.builder()
                                        .tool(step.getTool())
                                        .success(callResult.isSuccess())
                                        .result(result)
                                        .input(step.getInput())
                                        .error(
                                                callResult.isSuccess()
                                                        ?
                                                        null
                                                        :
                                                        "tool execute failed"
                                        )
                                        .build()
                        );

                log.info(
                        "tool={} success={} result={}",
                        step.getTool(),
                        callResult.isSuccess(),
                        result
                );



            }catch(Exception e){


                log.error(
                        "tool execute failed tool={}",
                        step.getTool(),
                        e
                );



                ToolCallResult failed =
                        ToolCallResult.failed(
                                step.getTool(),
                                e.getMessage()
                        );


                results.add(
                        failed
                );


                context.getObservations()
                        .add(
                                Observation.builder()
                                        .tool(step.getTool())
                                        .success(false)
                                        .input(step.getInput())
                                        .error(e.getMessage())
                                        .build()
                        );


            }


        }



        return results;

    }


    private boolean samePath(
            String a,
            String b
    ){

        if(a==null || b==null){
            return false;
        }


        return a.replace("\\","/")
                .equals(
                        b.replace("\\","/")
                );

    }

    private boolean shouldSkipDuplicateCreate(
            PlanStep step,
            AgentContext context
    ){

        if(!"create_file".equals(step.getTool())){

            return false;

        }


        if(!(step.getInput() instanceof com.minicodex.tool.ToolInput)){


            return false;

        }



        com.minicodex.tool.ToolInput input =
                (com.minicodex.tool.ToolInput) step.getInput();



        String path =
                input.getPath();



        if(path==null){


            return false;

        }



        if(context.getObservations()==null){


            return false;

        }



        return context.getObservations()
                .stream()
                .anyMatch(
                        o ->
                        {
                            if(!o.isSuccess()
                                    ||
                                    o.getResult()==null){

                                return false;
                            }


                            return samePath(
                                    o.getResult()
                                            .toString(),
                                    path
                            );
                        }
                );

    }












    private ToolCallResult buildToolResult(
            String tool,
            Object result
    ){

        if(result instanceof FileOperationResult){


            FileOperationResult fileResult =
                    (FileOperationResult) result;


            if(!fileResult.isSuccess()){


                return ToolCallResult.failed(
                        tool,
                        fileResult.getMessage()
                );

            }


            return ToolCallResult.success(
                    tool,
                    result
            );

        }




        if(result instanceof Map){


            Map map =
                    (Map) result;


            Object success =
                    map.get("success");


            if(success instanceof Boolean
                    &&
                    !((Boolean) success)){


                return ToolCallResult.failed(
                        tool,
                        String.valueOf(
                                map.get("message")
                        )
                );

            }

        }



        return ToolCallResult.success(
                tool,
                result
        );

    }



}
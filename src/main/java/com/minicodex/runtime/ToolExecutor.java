package com.minicodex.runtime;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.planner.CodePlan;
import com.minicodex.planner.PlanStep;
import com.minicodex.tool.AgentTool;
import com.minicodex.trace.TraceStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;


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


                log.info(
                        "trace steps size after tool={}",
                        context.getTrace().getSteps().size()
                );



                context.getObservations()
                        .add(
                                Observation.builder()
                                        .tool(step.getTool())
                                        .success(true)
                                        .result(result)
                                        .build()
                        );

                results.add(
                        ToolCallResult.success(
                                step.getTool(),
                                result
                        )
                );



            }catch(Exception e){

                context.getObservations()
                        .add(
                                Observation.builder()
                                        .tool(step.getTool())
                                        .success(false)
                                        .error(e.getMessage())
                                        .build()
                        );

                results.add(
                        ToolCallResult.failed(
                                step.getTool(),
                                e.getMessage()
                        )
                );

            }

        }



        return results;

    }



}
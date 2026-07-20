package com.minicodex.planner;


import com.minicodex.agent.AgentContext;
import com.minicodex.tool.ToolInput;
import org.springframework.stereotype.Component;



//@Component
public class SimplePlanner
        implements Planner {



    @Override
    public CodePlan createPlan(
            AgentContext context
    ){


        ToolInput input =
                new ToolInput();


        input.setPath(
                "."
        );



        PlanStep step =
                PlanStep.builder()
                        .order(1)
                        .tool("list_files")
                        .input(input)
                        .description(
                                "scan project files"
                        )
                        .build();



        return CodePlan.builder()
                .task(
                        context.getTask()
                )
                .steps(
                        java.util.Collections.singletonList(step)
                )
                .build();



    }


}
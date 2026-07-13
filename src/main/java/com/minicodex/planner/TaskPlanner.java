package com.minicodex.planner;


import org.springframework.stereotype.Component;



@Component
public class TaskPlanner {



    public TaskPlan plan(
            String input
    ){


        TaskPlan plan =
                new TaskPlan();


        plan.setGoal(
                input
        );


        /*
         后面这里交给LLM判断
         */


        plan.setNeedTool(true);



        return plan;

    }


}
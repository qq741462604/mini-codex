package com.minicodex.planner;


import com.minicodex.agent.AgentContext;



public interface Planner {



    CodePlan createPlan(
            AgentContext context
    );


}
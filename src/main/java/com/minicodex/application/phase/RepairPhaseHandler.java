package com.minicodex.application.phase;


import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.AgentPhase;
import org.springframework.stereotype.Component;
import com.minicodex.application.agent.Agent;


@Component
/**
 * RepairPhaseHandler：负责 Agent 阶段规则和阶段流转。
 * 所属层：应用层。
 *
 * @author yy
 */
public class RepairPhaseHandler
        implements AgentPhaseHandler {


    @Override
    public AgentPhase phase(){

        return AgentPhase.REPAIR;

    }



    @Override
    public AgentPhase next(
            AgentContext context
    ){

        if(context.getVerifyErrors()
                !=null
                &&
                !context.getVerifyErrors()
                        .isEmpty()){


            return AgentPhase.CODING;

        }


        return AgentPhase.FINISH;

    }

}
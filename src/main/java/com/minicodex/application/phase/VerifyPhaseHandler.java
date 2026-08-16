package com.minicodex.application.phase;


import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.AgentPhase;
import com.minicodex.application.verification.VerifyResult;
import org.springframework.stereotype.Component;
import com.minicodex.application.agent.Agent;


@Component
/**
 * VerifyPhaseHandler：负责 Agent 阶段规则和阶段流转。
 * 所属层：应用层。
 *
 * @author yy
 */
public class VerifyPhaseHandler
        implements AgentPhaseHandler {


    @Override
    public AgentPhase phase(){

        return AgentPhase.VERIFY;

    }



    @Override
    public AgentPhase next(
            AgentContext context
    ){
        Object result =
                context.getVariables()
                        .get("verify_result");


        if(result==null){

            return AgentPhase.VERIFY;

        }

        VerifyResult verifyResult =
                (VerifyResult)result;


        if(verifyResult.isSuccess()){

            return AgentPhase.FINISH;

        }


        return AgentPhase.REPAIR;




    }

}
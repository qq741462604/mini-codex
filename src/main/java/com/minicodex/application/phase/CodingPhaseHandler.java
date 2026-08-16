package com.minicodex.application.phase;


import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.AgentPhase;
import org.springframework.stereotype.Component;
import com.minicodex.application.agent.Agent;



@Component
/**
 * CodingPhaseHandler：负责 Agent 阶段规则和阶段流转。
 * 所属层：应用层。
 *
 * @author yy
 */
public class CodingPhaseHandler
        implements AgentPhaseHandler {



    @Override
    public AgentPhase phase(){

        return AgentPhase.CODING;

    }



    @Override
    public AgentPhase next(
            AgentContext context
    ){

        boolean failedChange =
                context.getLastObservations()
                        .stream()
                        .anyMatch(
                                o ->
                                        isFileChangeTool(o.getTool())
                                                &&
                                                !o.isSuccess()
                        );


        if(failedChange){

            return AgentPhase.CODING;

        }


        boolean changed =
                context.getLastObservations()
                        .stream()
                        .anyMatch(
                                o ->
                                        isFileChangeTool(o.getTool())
                                                &&
                                                o.isSuccess()
                        );





        if(changed){

            return AgentPhase.VERIFY;

        }


        return AgentPhase.CODING;

    }


    private boolean isFileChangeTool(
            String tool
    ){


        return "create_file".equals(tool)
                ||
                "write_file".equals(tool)
                ||
                "edit_file".equals(tool)
                ||
                "patch_file".equals(tool);

    }

}

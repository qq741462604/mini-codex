package com.minicodex.service;


import com.minicodex.agent.AgentContext;
import com.minicodex.tool.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class CodeModificationService {



    private final CreateFileTool createFileTool;



    private final WriteFileTool writeFileTool;



    private final PatchFileTool patchFileTool;




    public Object create(
            ToolInput input,
            AgentContext context
    ) throws Exception {


        return createFileTool.execute(
                input,
                context
        );

    }





    public Object write(
            ToolInput input,
            AgentContext context
    ) throws Exception {


        return writeFileTool.execute(
                input,
                context
        );

    }





    public Object patch(
            ToolInput input,
            AgentContext context
    ) throws Exception {


        return patchFileTool.execute(
                input,
                context
        );

    }



}
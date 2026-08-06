package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import com.minicodex.util.StreamUtil;
import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class RunTestTool
        extends BaseTool {


    private final WorkspaceService workspaceService;



    @Override
    public String name(){

        return "run_test";

    }




    @Override
    public String description(){

        return "run project test";

    }




    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {



        Process p =
                new ProcessBuilder(
                        "mvn",
                        "test"
                )
                        .directory(
                                workspaceService.getRoot()
                        )
                        .redirectErrorStream(true)
                        .start();


        return StreamUtil.read(
                p.getInputStream()
        );


    }


}

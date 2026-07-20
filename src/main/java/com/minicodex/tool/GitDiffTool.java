package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import com.minicodex.util.StreamUtil;
import org.springframework.stereotype.Component;



@Component
public class GitDiffTool
        extends BaseTool {



    @Override
    public String name(){

        return "git_diff";

    }



    @Override
    public String description(){

        return "show code changes";

    }




    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {



        Process p =
                Runtime.getRuntime()
                        .exec(
                                "git diff"
                        );


        return StreamUtil.read(
                p.getInputStream()
        );


    }


}
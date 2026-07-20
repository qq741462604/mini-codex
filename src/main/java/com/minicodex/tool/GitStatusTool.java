package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import com.minicodex.util.StreamUtil;
import org.springframework.stereotype.Component;


@Component
public class GitStatusTool
        extends BaseTool {


    @Override
    public String name() {

        return "git_status";

    }


    @Override
    public String description() {

        return "show git status";

    }


    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {


        Process p =
                Runtime.getRuntime()
                        .exec(
                                "git status"
                        );

//        return new String(
//                p.getInputStream()
//                        .readAllBytes()
//        );

        return StreamUtil.read(p.getInputStream());


    }

}
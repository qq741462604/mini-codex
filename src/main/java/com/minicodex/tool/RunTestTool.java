package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import com.minicodex.util.StreamUtil;
import org.springframework.stereotype.Component;



@Component
public class RunTestTool
        extends BaseTool {



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
                Runtime.getRuntime()
                        .exec(
                                "mvn test"
                        );


        return StreamUtil.read(
                p.getInputStream()
        );


    }


}
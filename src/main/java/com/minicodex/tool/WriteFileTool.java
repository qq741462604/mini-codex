package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;



@Component
public class WriteFileTool
        extends BaseTool {



    @Override
    public String name(){

        return "write_file";

    }



    @Override
    public String description(){

        return "overwrite file content";

    }




    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {



        ToolInput toolInput =
                (ToolInput) input;



        Files.write(
                Paths.get(
                        toolInput.getPath()
                ),
                toolInput.getContent()
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        );



        return "write success";

    }


}
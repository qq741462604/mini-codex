package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;



@Component
public class PatchFileTool
        extends BaseTool {



    @Override
    public String name(){

        return "patch_file";

    }



    @Override
    public String description(){

        return "replace text in file";

    }




    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {



        ToolInput toolInput =
                (ToolInput) input;



        String path =
                toolInput.getPath();



        String content =
                new String(
                        Files.readAllBytes(
                                Paths.get(path)
                        ),
                        StandardCharsets.UTF_8
                );



        if(!content.contains(
                toolInput.getOldText()
        )){


            throw new RuntimeException(
                    "old text not found"
            );

        }



        String result =
                content.replace(
                        toolInput.getOldText(),
                        toolInput.getNewText()
                );



        Files.write(
                Paths.get(path),
                result.getBytes(
                        StandardCharsets.UTF_8
                )
        );



        return "patch success";


    }


}
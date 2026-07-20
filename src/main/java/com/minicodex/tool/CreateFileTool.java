package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import org.springframework.stereotype.Component;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;



@Component
public class CreateFileTool
        extends BaseTool {



    @Override
    public String name(){

        return "create_file";

    }



    @Override
    public String description(){

        return "create new source file";

    }




    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {


        ToolInput toolInput =
                (ToolInput) input;



        File file =
                new File(
                        toolInput.getPath()
                );



        if(file.exists()){


            return FileOperationResult.builder()
                    .action("create")
                    .path(file.getPath())
                    .success(false)
                    .message(
                            "file already exists"
                    )
                    .build();


        }



        File parent =
                file.getParentFile();



        if(parent!=null){

            parent.mkdirs();

        }



        Files.write(
                file.toPath(),
                toolInput.getContent()
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        );



        return FileOperationResult.builder()
                .action("create")
                .path(file.getPath())
                .success(true)
                .message(
                        "create success"
                )
                .build();


    }


}
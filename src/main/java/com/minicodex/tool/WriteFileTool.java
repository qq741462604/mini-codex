package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;



@Component
@RequiredArgsConstructor
public class WriteFileTool
        extends BaseTool {


    private final WorkspaceService workspaceService;



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



        /*
         *
         * 关键修改
         *
         * 不允许直接 new File(path)
         *
         */
        File file =
                workspaceService.resolve(
                        toolInput.getPath()
                );



        File parent =
                file.getParentFile();



        if(parent!=null
                &&
                !parent.exists()){


            parent.mkdirs();

        }



        boolean exists =
                file.exists();



        Files.write(
                file.toPath(),
                toolInput.getContent()
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        );




        return FileOperationResult.builder()
                .action(
                        exists
                                ?
                                "update"
                                :
                                "create"
                )
                .path(
                        workspaceService.relativePath(file)
                )
                .success(true)
                .message(
                        exists
                                ?
                                "overwrite success"
                                :
                                "create success"
                )
                .build();


    }


}
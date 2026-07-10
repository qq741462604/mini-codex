package com.minicodex.tool.file;


import com.minicodex.tool.Tool;
import com.minicodex.tool.ToolResult;
import com.minicodex.common.WorkspaceConfig;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



@Component
public class ReadFileTool implements Tool {


    private final WorkspaceConfig config;



    public ReadFileTool(
            WorkspaceConfig config
    ){

        this.config = config;

    }



    @Override
    public String name(){

        return "read_file";

    }



    @Override
    public String description(){

        return "读取指定代码文件";

    }



    @Override
    public ToolResult execute(
            String input
    ){

        ToolResult result =
                new ToolResult();


        try {


            Path path =
                    Paths.get(
                            config.getPath(),
                            input
                    );


            String content =
                    new String(
                            Files.readAllBytes(path),
                            StandardCharsets.UTF_8
                    );


            result.setSuccess(true);

            result.setOutput(content);


        }catch(Exception e){


            result.setSuccess(false);

            result.setOutput(
                    e.getMessage()
            );

        }


        return result;

    }


}
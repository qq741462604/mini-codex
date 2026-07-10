package com.minicodex.tool.file;


import com.minicodex.common.WorkspaceConfig;
import com.minicodex.tool.Tool;
import com.minicodex.tool.ToolResult;
import org.springframework.stereotype.Component;


import java.nio.file.*;
import java.util.stream.Collectors;



@Component
public class ListFileTool implements Tool {



    private final WorkspaceConfig config;



    public ListFileTool(
            WorkspaceConfig config
    ){

        this.config=config;

    }



    @Override
    public String name(){

        return "list_file";

    }



    @Override
    public String description(){

        return "列出目录文件";

    }



    @Override
    public ToolResult execute(
            String input
    ){


        ToolResult result =
                new ToolResult();



        try {


            Path dir =
                    Paths.get(
                            config.getPath(),
                            input
                    );



            String files =
                    Files.walk(dir)
                            .map(Path::toString)
                            .collect(Collectors.joining("\n"));



            result.setSuccess(true);

            result.setOutput(files);



        }catch(Exception e){


            result.setSuccess(false);

            result.setOutput(
                    e.getMessage()
            );

        }



        return result;

    }



}
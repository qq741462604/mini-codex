package com.minicodex.tool.file;


import com.minicodex.common.WorkspaceConfig;
import com.minicodex.tool.Tool;
import com.minicodex.tool.ToolResult;
import org.springframework.stereotype.Component;


import java.nio.file.*;
import java.util.stream.Collectors;



@Component
public class SearchFileTool implements Tool {


    private final WorkspaceConfig config;



    public SearchFileTool(
            WorkspaceConfig config
    ){

        this.config=config;

    }



    @Override
    public String name(){

        return "search_file";

    }



    @Override
    public String description(){

        return "根据文件名搜索代码";

    }



    @Override
    public ToolResult execute(
            String input
    ){


        ToolResult result =
                new ToolResult();



        try {


            Path root =
                    Paths.get(
                            config.getPath()
                    );


            String output =
                    Files.walk(root)
                            .filter(
                                    p -> p.getFileName()
                                            .toString()
                                            .contains(input)
                            )
                            .map(
                                    Path::toString
                            )
                            .collect(
                                    Collectors.joining("\n")
                            );


            result.setSuccess(true);

            result.setOutput(output);



        }catch(Exception e){


            result.setSuccess(false);

            result.setOutput(
                    e.getMessage()
            );

        }


        return result;

    }

}
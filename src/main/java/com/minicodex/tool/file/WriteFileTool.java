package com.minicodex.tool.file;


import com.minicodex.common.WorkspaceConfig;
import com.minicodex.tool.Tool;
import com.minicodex.tool.ToolResult;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.nio.file.*;



@Component
public class WriteFileTool implements Tool {


    private final WorkspaceConfig config;



    public WriteFileTool(
            WorkspaceConfig config
    ){

        this.config=config;

    }



    @Override
    public String name(){

        return "write_file";

    }



    @Override
    public String description(){

        return "写入代码文件";

    }




    @Override
    public ToolResult execute(
            String input
    ){


        ToolResult result =
                new ToolResult();


        try {


            /*
             input格式:

             path|||content

             */


            String[] arr =
                    input.split(
                            "\\|\\|\\|",
                            2
                    );


            Path file =
                    Paths.get(
                            config.getPath(),
                            arr[0]
                    );


            Files.createDirectories(
                    file.getParent()
            );


            Files.write(
                    file,
                    arr[1].getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );


            result.setSuccess(true);

            result.setOutput(
                    "write success"
            );


        }catch(Exception e){


            result.setSuccess(false);

            result.setOutput(
                    e.getMessage()
            );

        }


        return result;

    }


}
package com.minicodex.infrastructure.tool;


import com.minicodex.domain.agent.AgentContext;
import com.minicodex.infrastructure.workspace.WorkspaceIgnoreMatcher;
import com.minicodex.infrastructure.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.minicodex.application.agent.Agent;
import com.minicodex.domain.tool.ToolInput;



@Component
@RequiredArgsConstructor
/**
 * ListFilesTool：提供受工作区约束的具体文件操作工具。
 * 所属层：基础设施层。
 *
 * @author yy
 */
public class ListFilesTool extends BaseTool {

    private final WorkspaceIgnoreMatcher matcher;

    private final WorkspaceService workspaceService;



    @Override
    public String name(){

        return "list_files";

    }



    @Override
    public String description(){

        return "列出指定目录下的文件结构";

    }




    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {


        logExecute(input);



        ToolInput toolInput =
                (ToolInput) input;



        String path =
                toolInput.getPath();



        if(path==null
                ||
                path.trim().isEmpty()){


            path=".";

        }



        File root =
                workspaceService.resolve(
                        path
                );



        if(!root.exists()){


            throw new RuntimeException(
                    "directory not found:"
                            + root.getAbsolutePath()
            );

        }



        List<String> result =
                new ArrayList<>();


        scan(
                root,
                result
        );


        return result;

    }





    private void scan(
            File file,
            List<String> result
    ){


        if(file==null
                ||
                !file.exists()){

            return;

        }

        if(matcher.match(file)){

            return;

        }

        if(file.isFile()){


            result.add(
                    workspaceService.relativePath(file)
            );


            return;

        }



        File[] files =
                file.listFiles();



        if(files==null){

            return;

        }



        for(File child:files){


            scan(
                    child,
                    result
            );

        }

    }


}
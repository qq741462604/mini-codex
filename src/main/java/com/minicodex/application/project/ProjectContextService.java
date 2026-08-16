package com.minicodex.application.project;


import com.minicodex.application.port.WorkspacePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.io.File;


@Service
@RequiredArgsConstructor
/**
 * ProjectContextService：负责构建和提供项目上下文。
 * 所属层：应用层。
 *
 * @author yy
 */
public class ProjectContextService {


    private final WorkspacePort workspacePort;



    public String buildSummary(){


        File root = workspacePort.getRoot();


        StringBuilder sb =
                new StringBuilder();



        sb.append("项目根目录:")
                .append(root.getAbsolutePath())
                .append("\n\n");


        scan(
                root,
                sb,
                0
        );


        return sb.toString();

    }




    private void scan(
            File file,
            StringBuilder sb,
            int level
    ){


        if(level>3){
            return;
        }


        File[] files =
                file.listFiles();



        if(files==null){
            return;
        }



        for(File f:files){


            for(int i=0;i<level;i++){

                sb.append("  ");

            }


            sb.append("- ")
                    .append(f.getName())
                    .append("\n");


            if(f.isDirectory()){

                scan(
                        f,
                        sb,
                        level+1
                );

            }


        }


    }


}

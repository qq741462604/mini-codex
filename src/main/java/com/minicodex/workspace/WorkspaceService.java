package com.minicodex.workspace;


import com.minicodex.config.WorkspaceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.io.File;
import java.nio.file.Path;



@Service
@RequiredArgsConstructor
public class WorkspaceService {


    private final WorkspaceProperties properties;




    public File getRoot(){


        return new File(
                properties.getRoot()
        );

    }






    public File resolve(
            String path
    ){


        File file =
                new File(path);



        if(file.isAbsolute()){

            return file;

        }



        return new File(
                getRoot(),
                path
        );


    }







    /**
     * 获取相对于workspace的路径
     *
     * 例如:
     *
     * D:/ideaProject/test/src/main/java/A.java
     *
     * 返回:
     *
     * src/main/java/A.java
     *
     */
    public String relativePath(
            File file
    ){


        try{


            Path root =
                    getRoot()
                            .toPath()
                            .toAbsolutePath()
                            .normalize();



            Path target =
                    file.toPath()
                            .toAbsolutePath()
                            .normalize();



            return root
                    .relativize(target)
                    .toString()
                    .replace("\\","/");



        }catch(Exception e){


            throw new RuntimeException(
                    "convert relative path failed:"
                            +
                            file.getAbsolutePath(),
                    e
            );


        }


    }


}
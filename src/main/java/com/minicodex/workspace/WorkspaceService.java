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


        if(path==null
                ||
                path.trim().isEmpty()){

            throw new IllegalArgumentException(
                    "workspace path empty"
            );

        }


        try{

            File root =
                    getRoot()
                            .getCanonicalFile();


            File file =
                    new File(path);


            File target =
                    file.isAbsolute()
                            ?
                            file.getCanonicalFile()
                            :
                            new File(
                                    root,
                                    path
                            ).getCanonicalFile();


            if(!isUnderRoot(
                    root,
                    target
            )){

                throw new IllegalArgumentException(
                        "path outside workspace: "
                                +
                                target.getAbsolutePath()
                );

            }


            return target;


        }catch(IllegalArgumentException e){

            throw e;

        }catch(Exception e){

            throw new RuntimeException(
                    "resolve workspace path failed:"
                            +
                            path,
                    e
            );

        }

    }


    private boolean isUnderRoot(
            File root,
            File target
    ) throws Exception {


        String rootPath =
                root.getCanonicalPath();


        String targetPath =
                target.getCanonicalPath();


        return targetPath.equals(rootPath)
                ||
                targetPath.startsWith(
                        rootPath
                                +
                                File.separator
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

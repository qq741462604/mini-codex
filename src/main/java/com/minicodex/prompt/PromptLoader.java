package com.minicodex.prompt;


import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;



@Service
@RequiredArgsConstructor
public class PromptLoader {


    private final WorkspaceService workspaceService;



    public String load(
            String name
    ){


        File file =
                workspaceService.resolve(
                        ".ai/prompts/"
                                + name
                                + ".md"
                );


        if(!file.exists()){

            throw new RuntimeException(
                    "prompt not found:"
                            + file.getAbsolutePath()
            );

        }



        try{


            return new String(
                    Files.readAllBytes(
                            file.toPath()
                    ),
                    StandardCharsets.UTF_8
            );


        }catch(Exception e){


            throw new RuntimeException(
                    "load prompt failed",
                    e
            );

        }


    }

}
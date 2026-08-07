package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;



@Component
@RequiredArgsConstructor
public class PatchFileTool
        extends BaseTool {


    private final WorkspaceService workspaceService;



    @Override
    public String name(){

        return "patch_file";

    }



    @Override
    public String description(){

        return "replace text in file";

    }


    private void validatePatch(
            ToolInput input,
            AgentContext context
    ){

        if(!wasFileRead(input,context)){

            throw new RuntimeException(
                    "patch_file requires read_file first"
            );
        }

    }


    private boolean wasFileRead(
            ToolInput input,
            AgentContext context
    ){

        File patchFile =
                workspaceService.resolve(
                        input.getPath()
                );


        for(Observation o: context.getObservations()){

            if(!o.isSuccess()){
                continue;
            }

            if(!"read_file".equals(o.getTool())){
                continue;
            }


            if(!(o.getResult() instanceof FileContent)){
                continue;
            }


            FileContent fc =
                    (FileContent)o.getResult();


            File readFile =
                    workspaceService.resolve(
                            fc.getPath()
                    );


            if(sameFile(
                    patchFile,
                    readFile
            )){

                return true;
            }

        }


        return false;
    }



    @Override
    public Object execute(
            Object input,
            AgentContext context
    ) throws Exception {



        ToolInput toolInput =
                (ToolInput) input;


        validatePatch(toolInput,context);

        String path =
                toolInput.getPath();


        File file =
                workspaceService.resolve(path);


        String content =
                new String(
                        Files.readAllBytes(
                                file.toPath()
                        ),
                        StandardCharsets.UTF_8
                );



        String lineSeparator =
                detectLineSeparator(content);


        String oldText =
                normalizeLineSeparator(
                        toolInput.getOldText(),
                        lineSeparator
                );


        String newText =
                normalizeLineSeparator(
                        toolInput.getNewText(),
                        lineSeparator
                );

        if(newText==null){

            throw new RuntimeException(
                    "newText is null"
            );

        }

        if(content.isEmpty()
                &&
                (oldText==null
                        ||
                        oldText.isEmpty())){

            Files.write(
                    file.toPath(),
                    newText.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            return FileOperationResult.builder()
                    .action("update")
                    .path(
                            workspaceService.relativePath(file)
                    )
                    .success(true)
                    .message("patch success")
                    .build();

        }


        boolean oldTextFound =
                oldText!=null
                        &&
                        content.contains(oldText);


        if(!oldTextFound){


            throw new RuntimeException(
                    "old text not found"
            );

        }



        String result =
                content.replace(
                        oldText,
                        newText
                );



        Files.write(
                file.toPath(),
                result.getBytes(
                        StandardCharsets.UTF_8
                )
        );



        return FileOperationResult.builder()
                .action("update")
                .path(
                        workspaceService.relativePath(file)
                )
                .success(true)
                .message("patch success")
                .build();


    }


    private String detectLineSeparator(
            String content
    ){


        if(content.contains("\r\n")){

            return "\r\n";

        }


        return "\n";

    }


    private String normalizeLineSeparator(
            String text,
            String lineSeparator
    ){


        if(text==null){

            return null;

        }


        return text
                .replace("\r\n","\n")
                .replace("\r","\n")
                .replace("\n",lineSeparator);

    }


    private boolean sameFile(
            File left,
            File right
    ){


        try{

            return left.getCanonicalFile()
                    .equals(
                            right.getCanonicalFile()
                    );

        }catch(Exception e){

            return false;

        }

    }


}

package com.minicodex.tool;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;



@Component
public class PatchFileTool
        extends BaseTool {



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

        if(input.getOldText()==null
                ||
                input.getOldText().trim().isEmpty()){

            throw new RuntimeException(
                    "patch_file requires oldText"
            );
        }



        boolean fromReadFile=false;


        for(Observation o:
                context.getObservations()){


            if(!o.isSuccess()){
                continue;
            }


            if(!"read_file".equals(o.getTool())){
                continue;
            }


            if(o.getResult()!=null
                    &&
                    o.getResult().toString()
                            .contains(input.getOldText())){


                fromReadFile=true;
                break;
            }

        }



        if(!fromReadFile){

            throw new RuntimeException(
                    "patch_file oldText must come from read_file"
            );

        }

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



        String content =
                new String(
                        Files.readAllBytes(
                                Paths.get(path)
                        ),
                        StandardCharsets.UTF_8
                );



        if(!content.contains(
                toolInput.getOldText()
        )){


            throw new RuntimeException(
                    "old text not found"
            );

        }



        String result =
                content.replace(
                        toolInput.getOldText(),
                        toolInput.getNewText()
                );



        Files.write(
                Paths.get(path),
                result.getBytes(
                        StandardCharsets.UTF_8
                )
        );



        return "patch success";


    }


}
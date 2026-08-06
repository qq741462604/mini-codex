package com.minicodex.verify;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.tool.AgentTool;
import com.minicodex.tool.ToolInput;
import com.minicodex.runtime.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.HashSet;
import java.util.Set;



@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyFileLoader {


    private final ToolRegistry toolRegistry;



    public void loadChangedFiles(
            AgentContext context
    ){


        AgentTool readTool =
                toolRegistry.getTool(
                        "read_file"
                );


        if(readTool==null){

            log.warn(
                    "read_file tool not found"
            );

            return;

        }



        Set<String> paths =
                extractPaths(
                        context
                );



        for(String path:paths){


            try{


                ToolInput input =
                        ToolInput.builder().path(path).build();





                Object result =
                        readTool.execute(
                                input,
                                context
                        );


                context.getObservations()
                        .add(
                                Observation.builder()
                                        .tool("read_file")
                                        .success(true)
                                        .input(input)
                                        .result(result)
                                        .build()
                        );


                log.info(
                        "verify load file={}",
                        path
                );



            }catch(Exception e){


                log.error(
                        "verify read file failed path={}",
                        path,
                        e
                );


            }


        }


    }




    private Set<String> extractPaths(
            AgentContext context
    ){


        Set<String> paths =
                new HashSet<>();


        for(Observation o:
                context.getObservations()){


            if(!o.isSuccess()){

                continue;

            }


            if(!"create_file".equals(o.getTool())
                    &&
                    !"write_file".equals(o.getTool())
                    &&
                    !"edit_file".equals(o.getTool())
                    &&
                    !"patch_file".equals(o.getTool())
            ){

                continue;

            }


            if(o.getResult()==null){

                continue;

            }



            String text =
                    o.getResult()
                            .toString();


            if(text.contains("path=")){


                int start =
                        text.indexOf("path=")
                                +
                                5;


                int end =
                        text.indexOf(
                                ",",
                                start
                        );


                if(end<0){

                    end=text.length();

                }


                paths.add(
                        text.substring(
                                start,
                                end
                        )
                );

            }


        }


        return paths;

    }


}

package com.minicodex.verify;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.tool.FileContent;
import com.minicodex.tool.ToolInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class CodeValidator {


    public List<String> validate(
            AgentContext context
    ){


        List<String> errors =
                new ArrayList<>();



        if(context.getObservations()==null){

            return errors;

        }



        List<Observation> observations =
                context.getObservations();


        for(int i=0;i<observations.size();i++){


            Observation o =
                    observations.get(i);


            validateFailedChange(
                    o,
                    i,
                    observations,
                    errors
            );


            if(!o.isSuccess()){

                continue;

            }



            validateFile(
                    o,
                    errors
            );


        }


        return errors;

    }


    private void validateFailedChange(
            Observation observation,
            int index,
            List<Observation> observations,
            List<String> errors
    ){


        if(observation.isSuccess()){

            return;

        }


        if(!isFileChangeTool(
                observation.getTool()
        )){

            return;

        }


        if(hasLaterSuccessfulChange(
                observation,
                index,
                observations
        )){

            return;

        }


        errors.add(
                "文件修改失败 tool="
                        +
                        observation.getTool()
                        +
                        " path="
                        +
                        getInputPath(observation)
                        +
                        " error="
                        +
                        observation.getError()
        );

    }


    private boolean hasLaterSuccessfulChange(
            Observation failed,
            int index,
            List<Observation> observations
    ){


        String failedPath =
                getInputPath(failed);


        if(failedPath==null){

            return false;

        }


        for(int i=index+1;i<observations.size();i++){


            Observation current =
                    observations.get(i);


            if(!current.isSuccess()
                    ||
                    !isFileChangeTool(current.getTool())){

                continue;

            }


            if(failedPath.equals(
                    getInputPath(current)
            )){

                return true;

            }

        }


        return false;

    }


    private boolean isFileChangeTool(
            String tool
    ){


        return "create_file".equals(tool)
                ||
                "write_file".equals(tool)
                ||
                "edit_file".equals(tool)
                ||
                "patch_file".equals(tool);

    }


    private String getInputPath(
            Observation observation
    ){


        if(observation.getInput()
                instanceof ToolInput){

            return ((ToolInput) observation.getInput())
                    .getPath();

        }


        return null;

    }





    private void validateFile(
            Observation observation,
            List<String> errors
    ){


        if(!"read_file".equals(
                observation.getTool()
        )){


            return;

        }



        if(!(observation.getResult()
                instanceof FileContent)){


            return;

        }



        FileContent file =
                (FileContent)
                        observation.getResult();



        String path =
                file.getPath();



        String content =
                String.join(
                        "\n",
                        file.getLines()
                );



        if(path.endsWith("Controller.java")){


            if(!content.contains(
                    "@RestController"
            )){


                errors.add(
                        path
                                +
                                " Controller缺少@RestController"
                );

            }


        }





        if(path.endsWith("Service.java")){


            if(!content.contains(
                    "@Service"
            )){


                errors.add(
                        path
                                +
                                " Service缺少@Service"
                );

            }


        }


    }


}

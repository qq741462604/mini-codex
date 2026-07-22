package com.minicodex.verify;


import com.minicodex.agent.AgentContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.tool.FileContent;
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



        for(Observation o:
                context.getObservations()){


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
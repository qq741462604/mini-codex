package com.minicodex.ai;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;



@Component
public class ToolCallParser {



    private final ObjectMapper mapper =
            new ObjectMapper();





    public ToolCall parse(
            String content
    ){


        try {


            if(content == null){
                return null;
            }



            String text =
                    content.trim();



            if(!text.startsWith("{")){

                return null;

            }



            ToolCall call =
                    mapper.readValue(
                            text,
                            ToolCall.class
                    );



            if(!call.isCallTool()){

                return null;

            }



            return call;



        }catch(Exception e){


            return null;

        }


    }


}
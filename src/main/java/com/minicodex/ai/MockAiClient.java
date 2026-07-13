package com.minicodex.ai;


import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;


import java.util.List;


//@Primary
//@Component
public class MockAiClient
        implements AiClient {



    private int count=0;



    @Override
    public AiResponse chat(
            List<AiMessage> messages
    ){


        AiResponse response =
                new AiResponse();



        if(count==0){


            ToolCall call =
                    new ToolCall();


            call.setCallTool(true);

            call.setToolName(
                    "read_file"
            );


            call.setArguments(
                    "test/UserController.java"
            );


            response.setToolCall(
                    call
            );


            count++;

        }
        else{


            response.setContent(
                    "文件读取完成，代码分析结束"
            );


        }


        return response;

    }


}
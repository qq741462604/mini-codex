package com.minicodex.ai;


import lombok.extern.slf4j.Slf4j;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;



import java.util.List;



@Slf4j
@Component
@Profile("test")
public class FakeQwenAiClient
        implements AiClient {



    @Override
    public AiResponse chat(
            List<AiMessage> messages
    ){



        log.info(
                "收到消息数量:{}",
                messages.size()
        );



        boolean hasToolResult =
                false;



        for(AiMessage message:messages){


            if("tool".equals(
                    message.getRole()
            )){


                hasToolResult=true;

                break;

            }

        }





        AiResponse response =
                new AiResponse();



        if(!hasToolResult){


            log.info(
                    "模拟AI请求文件"
            );


            ToolCall call =
                    new ToolCall();


            call.setCallTool(
                    true
            );


            call.setToolName(
                    "read_file"
            );


            call.setArguments(
                    "test/UserController.java"
            );


            response.setToolCall(
                    call
            );


        }
        else{


            log.info(
                    "模拟AI分析完成"
            );


            response.setContent(
                    "UserController分析完成"
            );

        }



        return response;


    }


}
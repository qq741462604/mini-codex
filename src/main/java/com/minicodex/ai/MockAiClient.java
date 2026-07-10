package com.minicodex.ai;


import org.springframework.stereotype.Component;


import java.util.List;


@Component
public class MockAiClient implements AiClient {



    @Override
    public AiResponse chat(
            List<AiMessage> messages
    ){


        AiResponse response =
                new AiResponse();


        response.setContent(
                "Mock AI Response"
        );


        response.setNeedTool(false);


        return response;

    }


}
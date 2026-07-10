package com.minicodex.agent;


import com.minicodex.ai.*;
import org.springframework.stereotype.Component;


import java.util.*;


@Component
public class Agent {


    private final AiClient aiClient;



    public Agent(
            AiClient aiClient
    ){

        this.aiClient = aiClient;

    }




    public String run(
            String input
    ){


        AiMessage message =
                new AiMessage();


        message.setRole(
                "user"
        );


        message.setContent(
                input
        );


        AiResponse response =
                aiClient.chat(
                        Collections.singletonList(message)
                );


        return response.getContent();


    }


}
package com.minicodex.ai;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;


import java.util.*;



@Component
public class QwenAiClient
        implements AiClient {



    private final AiConfig config;


    private final AiHttpClient httpClient;

    private final ToolCallParser parser;

    private final ObjectMapper mapper =
            new ObjectMapper();




    public QwenAiClient(
            AiConfig config,
            AiHttpClient httpClient,
            ToolCallParser parser
    ){

        this.config=config;

        this.httpClient=httpClient;

        this.parser=parser;

    }





    @Override
    public AiResponse chat(
            List<AiMessage> messages
    ){


        try {


            Map<String,Object> request =
                    new HashMap<>();


            request.put(
                    "model",
                    config.getModel()
            );


            request.put(
                    "messages",
                    messages
            );


            request.put(
                    "temperature",
                    0.2
            );



            String json =
                    mapper.writeValueAsString(
                            request
                    );



            String result =
                    httpClient.post(
                            config.getUrl(),
                            json,
                            config.getApiKey()
                    );



            return parse(result);



        }catch(Exception e){


            throw new RuntimeException(
                    e
            );

        }


    }






    private AiResponse parse(
            String json
    ) throws Exception {



        JsonNode root =
                mapper.readTree(json);



        String content =
                root.path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();



        AiResponse response =
                new AiResponse();



        ToolCall call =
                parser.parse(
                        content
                );



        if(call!=null){


            response.setToolCall(
                    call
            );


        }
        else{


            response.setContent(
                    content
            );


        }



        return response;


    }



}
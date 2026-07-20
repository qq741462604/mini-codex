package com.minicodex.llm;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import okhttp3.*;

import java.util.Collections;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class QwenClient
        implements LlmClient {



    private final ObjectMapper objectMapper;



//    private final OkHttpClient client =
//            new OkHttpClient();

    private final OkHttpClient client =
            new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();




    @Value("${llm.api.url}")
    private String url;



    @Value("${llm.api.key}")
    private String apiKey;



    @Value("${llm.model:qwen-plus}")
    private String model;




    @Override
    public String chat(
            String prompt
    ){


        try{


            LlmRequest request =
                    new LlmRequest();


            request.setModel(model);


            request.setMessages(
                    Collections.singletonList(
                            new LlmRequest.Message(
                                    "user",
                                    prompt
                            )
                    )
            );



            String json =
                    objectMapper.writeValueAsString(
                            request
                    );



            Request httpRequest =
                    new Request.Builder()
                            .url(url)
                            .addHeader(
                                    "Authorization",
                                    "Bearer "
                                            + apiKey
                            )
                            .post(
                                    RequestBody.create(
                                            MediaType.parse(
                                                    "application/json"
                                            ),
                                            json
                                    )
                            )
                            .build();



            Response response =
                    client.newCall(
                            httpRequest
                    ).execute();



            String body =
                    response.body()
                            .string();



            JsonNode node =
                    objectMapper.readTree(
                            body
                    );



            /*
             Qwen格式:

             output.text

             或

             choices[0].message.content

             */


            if(node.has("output")){

                return node
                        .get("output")
                        .asText();

            }


            if(node.has("choices")){

                return node
                        .get("choices")
                        .get(0)
                        .get("message")
                        .get("content")
                        .asText();

            }



            return body;



        }catch(Exception e){


            throw new RuntimeException(
                    "call qwen failed",
                    e
            );

        }


    }


}
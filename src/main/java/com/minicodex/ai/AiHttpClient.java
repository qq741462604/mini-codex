package com.minicodex.ai;


import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
public class AiHttpClient {


    private final OkHttpClient client =
            new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();



    public String post(
            String url,
            String json,
            String apiKey
    ) throws Exception {


        RequestBody body =
                RequestBody.create(
                        json,
                        MediaType.parse(
                                "application/json;charset=utf-8"
                        )
                );



        Request request =
                new Request.Builder()
                        .url(url)
                        .addHeader(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .post(body)
                        .build();



        Response response =
                client.newCall(request)
                        .execute();



        if(!response.isSuccessful()){

            throw new RuntimeException(
                    "AI请求失败:"
                            +response.code()
            );

        }



        return response.body()
                .string();

    }


}
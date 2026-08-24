package com.minicodex.infrastructure.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicodex.application.port.LlmClient;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * QwenClient：提供大模型调用的具体适配实现。 所属层：基础设施层。
 *
 * @author yy
 */
public class QwenClient implements LlmClient {

  private static final int READ_TIMEOUT_SECONDS = 300;

  private final ObjectMapper objectMapper;

  //    private final OkHttpClient client =
  //            new OkHttpClient();

  private OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(60, TimeUnit.SECONDS)
          .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
          .writeTimeout(30, TimeUnit.SECONDS)
          .build();

  @Value("${llm.api.url}")
  private String url;

  @Value("${llm.api.key}")
  private String apiKey;

  @Value("${llm.model:qwen-plus}")
  private String model;

  @Override
  public String chat(String prompt) {

    try {
      long start = System.currentTimeMillis();
      log.info(
          "qwen request model={} promptLength={}", model, prompt == null ? 0 : prompt.length());

      LlmRequest request = new LlmRequest();

      request.setModel(model);

      request.setMessages(Collections.singletonList(new LlmRequest.Message("user", prompt)));

      String json = objectMapper.writeValueAsString(request);

      Request httpRequest =
          new Request.Builder()
              .url(url)
              .addHeader("Authorization", "Bearer " + apiKey)
              .post(RequestBody.create(MediaType.parse("application/json"), json))
              .build();

      Response response = client.newCall(httpRequest).execute();

      String body = response.body().string();

      log.info(
          "qwen response cost={}ms bodyLength={}",
          System.currentTimeMillis() - start,
          body.length());

      JsonNode node = objectMapper.readTree(body);

      /*
      Qwen格式:

      output.text

      或

      choices[0].message.content

      */

      if (node.has("output")) {

        JsonNode output = node.get("output");

        if (output.has("text")) {

          return output.get("text").asText();
        }

        if (output.isTextual()) {

          return output.asText();
        }
      }

      if (node.has("choices")) {

        return node.get("choices").get(0).get("message").get("content").asText();
      }

      return body;

    } catch (Exception e) {
      log.error(
          "qwen call failed url={} model={} timeout={}ms",
          url,
          model,
          READ_TIMEOUT_SECONDS * 1000,
          e);

      throw new RuntimeException("call qwen failed", e);
    }
  }
}

package com.minicodex.infrastructure.llm;

import java.util.List;
import lombok.Data;

@Data
/**
 * LlmRequest：提供大模型调用的具体适配实现。 所属层：基础设施层。
 *
 * @author yy
 */
public class LlmRequest {

  private String model;

  private List<Message> messages;

  @Data
  public static class Message {

    private String role;

    private String content;

    public Message(String role, String content) {

      this.role = role;
      this.content = content;
    }
  }
}

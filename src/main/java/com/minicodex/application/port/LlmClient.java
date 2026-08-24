package com.minicodex.application.port;

/**
 * LlmClient：定义应用层调用外部能力的抽象边界。 所属层：应用层。
 *
 * @author yy
 */
public interface LlmClient {

  String chat(String prompt);
}

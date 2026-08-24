package com.minicodex.application.port;

/**
 * 定义应用层读取 Prompt 模板的边界。 所属层：应用层。
 *
 * @author yy
 */
public interface PromptRepository {

  String load(String name);
}

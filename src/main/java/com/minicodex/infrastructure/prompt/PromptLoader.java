package com.minicodex.infrastructure.prompt;

import com.minicodex.application.port.PromptRepository;
import com.minicodex.infrastructure.config.AgentHomeService;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * PromptLoader：提供 Prompt 模板加载与渲染能力。 所属层：基础设施层。
 *
 * @author yy
 */
public class PromptLoader implements PromptRepository {

  private final AgentHomeService agentHomeService;

  public String load(String name) {

    File file = agentHomeService.resolve(".ai/prompts/" + name + ".md");

    if (!file.exists()) {

      throw new RuntimeException("prompt not found:" + file.getAbsolutePath());
    }

    try {

      return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

    } catch (Exception e) {

      throw new RuntimeException("load prompt failed", e);
    }
  }
}

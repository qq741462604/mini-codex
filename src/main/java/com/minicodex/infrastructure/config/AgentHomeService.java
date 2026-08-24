package com.minicodex.infrastructure.config;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
/**
 * AgentHomeService：提供外部配置的读取与绑定能力。 所属层：基础设施层。
 *
 * @author yy
 */
public class AgentHomeService {

  private final String home;

  public AgentHomeService(@Value("${minicodex.home}") String home) {

    this.home = home;
  }

  public File resolve(String path) {

    return new File(home, path);
  }
}

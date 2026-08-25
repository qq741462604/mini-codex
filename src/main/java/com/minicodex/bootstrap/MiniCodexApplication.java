package com.minicodex.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MiniCodexApplication：负责 Spring Boot 启动与依赖装配。 所属层：启动装配。
 *
 * @author yy
 */
@SpringBootApplication(scanBasePackages = "com.minicodex")
@MapperScan("com.minicodex.infrastructure.memory")
public class MiniCodexApplication {

  public static void main(String[] args) throws IOException {

    if (isLocalProfile(args)) {
      resetLogFile();
    }

    SpringApplication.run(MiniCodexApplication.class, args);
  }

  private static void resetLogFile() throws IOException {
    Files.createDirectories(Paths.get("logs"));
    Files.write(
        Paths.get("logs", "mini-codex.log"),
        new byte[0],
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

  private static boolean isLocalProfile(String[] args) {
    return containsLocalProfile(System.getProperty("spring.profiles.active"))
        || containsLocalProfile(System.getenv("SPRING_PROFILES_ACTIVE"))
        || Arrays.stream(args == null ? new String[0] : args)
            .filter(arg -> arg != null && arg.startsWith("--spring.profiles.active="))
            .map(arg -> arg.substring("--spring.profiles.active=".length()))
            .anyMatch(MiniCodexApplication::containsLocalProfile)
        || noProfileSpecified(args);
  }

  private static boolean noProfileSpecified(String[] args) {
    boolean hasArgsProfile =
        Arrays.stream(args == null ? new String[0] : args)
            .anyMatch(arg -> arg != null && arg.startsWith("--spring.profiles.active="));
    return !hasArgsProfile
        && System.getProperty("spring.profiles.active") == null
        && System.getenv("SPRING_PROFILES_ACTIVE") == null;
  }

  private static boolean containsLocalProfile(String profiles) {
    if (profiles == null || profiles.trim().isEmpty()) {
      return false;
    }
    return Arrays.stream(profiles.split(",")).map(String::trim).anyMatch("local"::equals);
  }
}

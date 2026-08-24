package com.minicodex.application.verification;

import com.minicodex.application.execution.ToolRegistry;
import com.minicodex.application.port.AgentTool;
import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.Observation;
import com.minicodex.domain.tool.ToolInput;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * VerifyFileLoader：负责代码变更后的验证与修复反馈。 所属层：应用层。
 *
 * @author yy
 */
public class VerifyFileLoader {

  private final ToolRegistry toolRegistry;

  public void loadChangedFiles(AgentContext context) {
    AgentTool readTool = toolRegistry.getTool("read_file");
    if (readTool == null) {
      log.warn("read_file tool not found");
      return;
    }

    Set<String> paths = extractPaths(context);
    for (String path : paths) {
      try {
        ToolInput input = ToolInput.builder().path(path).startLine(1).endLine(10000).build();
        Object result = readTool.execute(input, context);
        context
            .getObservations()
            .add(
                Observation.builder()
                    .tool("read_file")
                    .success(true)
                    .input(input)
                    .result(result)
                    .build());
      } catch (Exception e) {
        log.error("verify read file failed path={}", path, e);
      }
    }
  }

  private Set<String> extractPaths(AgentContext context) {
    Set<String> paths = new HashSet<>();
    for (Observation observation : context.getObservations()) {
      if (!isChangedFileObservation(observation) || observation.getResult() == null) {
        continue;
      }
      String text = observation.getResult().toString();
      if (text.contains("path=")) {
        int start = text.indexOf("path=") + 5;
        int end = text.indexOf(",", start);
        if (end < 0) {
          end = text.length();
        }
        paths.add(text.substring(start, end));
      }
    }
    return paths;
  }

  private boolean isChangedFileObservation(Observation observation) {
    if (!observation.isSuccess()) {
      return false;
    }
    String tool = observation.getTool();
    return "create_file".equals(tool)
        || "write_file".equals(tool)
        || "edit_file".equals(tool)
        || "patch_file".equals(tool);
  }
}

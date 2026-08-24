package com.minicodex.application.execution;

import com.minicodex.application.port.AgentTool;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
/**
 * ToolRegistry：负责 Agent 执行流程中的协调与结果汇总。 所属层：应用层。
 *
 * @author yy
 */
public class ToolRegistry {

  private final Map<String, AgentTool> tools = new HashMap<>();

  public ToolRegistry(List<AgentTool> toolList) {

    for (AgentTool tool : toolList) {

      tools.put(tool.name(), tool);
    }
  }

  public AgentTool getTool(String name) {

    return tools.get(name);
  }
}

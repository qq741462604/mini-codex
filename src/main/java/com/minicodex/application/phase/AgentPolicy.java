package com.minicodex.application.phase;

import com.minicodex.domain.agent.AgentPhase;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
/**
 * AgentPolicy：负责 Agent 阶段规则和阶段流转。 所属层：应用层。
 *
 * @author yy
 */
public class AgentPolicy {

  private static final Map<AgentPhase, Set<String>> RULES = new HashMap<>();

  static {
    RULES.put(
        AgentPhase.ANALYSIS,
        new HashSet<>(Arrays.asList("list_files", "search_code", "read_file")));

    RULES.put(
        AgentPhase.CODING,
        new HashSet<>(
            Arrays.asList(
                "create_file",
                "write_file",
                "patch_file",
                "edit_file",
                "read_file",
                "search_code")));

    RULES.put(AgentPhase.VERIFY, new HashSet<>(Arrays.asList("read_file", "search_code")));

    RULES.put(
        AgentPhase.REPAIR,
        new HashSet<>(
            Arrays.asList("read_file", "search_code", "write_file", "patch_file", "edit_file")));
  }

  public boolean allow(AgentPhase phase, String tool) {

    Set<String> tools = RULES.get(phase);

    if (tools == null) {

      return false;
    }

    return tools.contains(tool);
  }
}

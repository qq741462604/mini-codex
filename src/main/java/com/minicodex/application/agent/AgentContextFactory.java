package com.minicodex.application.agent;

import com.minicodex.application.port.MemoryStore;
import com.minicodex.application.port.SkillRepository;
import com.minicodex.domain.agent.AgentContext;
import com.minicodex.domain.agent.AgentPhase;
import com.minicodex.domain.memory.Memory;
import com.minicodex.domain.skill.Skill;
import com.minicodex.domain.trace.AgentTrace;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Agent上下文工厂。
 *
 * @author yy
 * @date 2026-08-10 00:00:00
 */
@Component
@RequiredArgsConstructor
public class AgentContextFactory {

  private final MemoryStore memoryStore;
  private final SkillRepository skillLoader;

  public AgentContext create(String agentId, String task, AgentTrace trace) {
    return AgentContext.builder()
        .agentId(agentId)
        .task(task)
        .trace(trace)
        .phase(AgentPhase.ANALYSIS)
        .memories(safeMemoryQuery(task))
        .observations(new ArrayList<>())
        .skills(loadSkills())
        .build();
  }

  private List<Memory> safeMemoryQuery(String task) {
    List<Memory> memories = memoryStore.query(task);
    return memories == null ? new ArrayList<>() : memories;
  }

  private List<Skill> loadSkills() {
    List<Skill> skills = skillLoader.load();
    return skills == null ? new ArrayList<>() : skills;
  }
}

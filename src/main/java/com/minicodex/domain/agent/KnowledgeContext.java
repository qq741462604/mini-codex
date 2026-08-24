package com.minicodex.domain.agent;

import com.minicodex.domain.memory.Memory;
import com.minicodex.domain.skill.Skill;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * KnowledgeContext：承载 Agent 运行状态、观察结果与领域数据。 所属层：领域层。
 *
 * @author yy
 */
public class KnowledgeContext {

  /** 技能规则 */
  private List<Skill> skills;

  /** 历史记忆 */
  private List<Memory> memories;

  /** 项目规则 */
  private List<String> projectRules;
}

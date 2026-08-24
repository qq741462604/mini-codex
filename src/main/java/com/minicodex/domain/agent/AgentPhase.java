package com.minicodex.domain.agent;

/**
 * AgentPhase：承载 Agent 运行状态、观察结果与领域数据。 所属层：领域层。
 *
 * @author yy
 */
public enum AgentPhase {

  /** 分析项目阶段 */
  ANALYSIS,

  /** 代码生成阶段 */
  CODING,

  /** 验证阶段 */
  VERIFY,

  /** 修复阶段 */
  REPAIR,

  /** 完成 */
  FINISH
}

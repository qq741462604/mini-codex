package com.minicodex.domain.memory;

/**
 * MemoryType：承载 Agent 记忆领域数据与分类。 所属层：领域层。
 *
 * @author yy
 */
public enum MemoryType {

  /** 普通任务记录 */
  TASK,

  /** 项目规范 */
  PROJECT_RULE,

  /** 验证失败记录 */
  VERIFY_ERROR,

  /** 成功方案 */
  SUCCESS_PATTERN
}

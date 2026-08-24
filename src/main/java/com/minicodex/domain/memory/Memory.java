package com.minicodex.domain.memory;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Memory：承载 Agent 记忆领域数据与分类。 所属层：领域层。
 *
 * @author yy
 */
public class Memory {

  /** 唯一key */
  private String key;

  /** 记忆内容 */
  private String content;

  /**
   * 类型
   *
   * <p>TASK PROJECT_RULE VERIFY_ERROR SUCCESS_PATTERN
   */
  private MemoryType type;

  /** 创建时间 */
  @Builder.Default private Date createTime = new Date();
}

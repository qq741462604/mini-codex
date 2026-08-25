package com.minicodex.infrastructure.memory;

import java.util.Date;
import lombok.Data;

/**
 * MemoryRecord：承载记忆持久化字段。 所属层：基础设施层。
 *
 * @author summer
 * @date 2026-08-25 00:00:00
 */
@Data
public class MemoryRecord {

  /**
   * 记忆唯一标识。
   */
  private String key;

  /**
   * 记忆内容。
   */
  private String content;

  /**
   * 记忆类型。
   */
  private String type;

  /**
   * 创建时间。
   */
  private Date createTime;
}

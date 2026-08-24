package com.minicodex.domain.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * ContextItem：承载上下文条目的领域数据。 所属层：领域层。
 *
 * @author yy
 */
public class ContextItem {

  /** 来源 */
  private String source;

  /** 内容 */
  private String content;

  /** 优先级 */
  private int priority;

  /** token估算 */
  private int tokens;
}

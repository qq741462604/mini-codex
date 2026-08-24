package com.minicodex.interfaces.web;

import java.util.List;
import lombok.Data;

@Data
/**
 * AgentRequest：负责 Agent HTTP 请求与响应的协议适配。 所属层：接口层。
 *
 * @author yy
 */
public class AgentRequest {

  /** 单条需求内容。 */
  private String task;

  /** 批量需求列表。 */
  private List<AgentTaskRequest> tasks;
}

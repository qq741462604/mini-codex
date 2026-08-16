package com.minicodex.domain.agent;

import com.minicodex.application.agent.Agent;

/**
 * AgentStatus：承载 Agent 运行状态、观察结果与领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public enum AgentStatus {

    IDLE,

    ANALYZING,

    CODING,

    VERIFYING,

    FINISHED

}
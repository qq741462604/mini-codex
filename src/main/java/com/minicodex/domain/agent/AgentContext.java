package com.minicodex.domain.agent;


import com.minicodex.domain.agent.KnowledgeContext;
import com.minicodex.domain.agent.Observation;
import com.minicodex.domain.memory.Memory;
import com.minicodex.domain.project.ProjectIndex;
import com.minicodex.domain.skill.Skill;
import com.minicodex.domain.trace.AgentTrace;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.minicodex.application.agent.Agent;



@Data
@Builder
/**
 * AgentContext：承载 Agent 运行状态、观察结果与领域数据。
 * 所属层：领域层。
 *
 * @author yy
 */
public class AgentContext {


    private String agentId;



    private String task;

    private ProjectIndex projectIndex;

//    private String workspace;

    private AgentTrace trace;

    private String lastPlanHash;

    @Builder.Default
    private Map<String,Object> variables =
            new HashMap<>();



    @Builder.Default
    private List<Memory> memories =
            new ArrayList<>();

    private KnowledgeContext knowledge;
    /**
     * 工具观察结果
     */
    @Builder.Default
    private List<Observation> observations =
            new ArrayList<>();
    @Builder.Default
    private List<Observation> lastObservations =
            new ArrayList<>();

    @Builder.Default
    private List<Skill> skills =
            new ArrayList<>();

    @Builder.Default
    private AgentStatus status =
            AgentStatus.IDLE;

    /**
     * Agent当前执行阶段
     */
    @Builder.Default
    private AgentPhase phase =
            AgentPhase.ANALYSIS;

    /**
     * 验证失败原因
     */
    @Builder.Default
    private List<String> verifyErrors =
            new ArrayList<>();

}
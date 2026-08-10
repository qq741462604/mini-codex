package com.minicodex.agent;


import com.minicodex.agent.knowledge.KnowledgeContext;
import com.minicodex.agent.observation.Observation;
import com.minicodex.memory.Memory;
import com.minicodex.project.ProjectIndex;
import com.minicodex.skill.Skill;
import com.minicodex.trace.AgentTrace;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Data
@Builder
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
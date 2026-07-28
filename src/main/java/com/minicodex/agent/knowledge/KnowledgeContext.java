package com.minicodex.agent.knowledge;


import com.minicodex.memory.Memory;
import com.minicodex.skill.Skill;
import lombok.Builder;
import lombok.Data;


import java.util.List;



@Data
@Builder
public class KnowledgeContext {


    /**
     * 技能规则
     */
    private List<Skill> skills;



    /**
     * 历史记忆
     */
    private List<Memory> memories;



    /**
     * 项目规则
     */
    private List<String> projectRules;


}
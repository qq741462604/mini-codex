package com.minicodex.controller;


import com.minicodex.agent.Agent;
import com.minicodex.agent.AgentResult;
import com.minicodex.skill.SkillMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {



    private final Agent agent;

    private final SkillMatcher skillMatcher;



    @PostMapping("/run")
    public AgentResult run(
            @RequestBody AgentRequest request
    ){

        String task = request == null ? null : request.getTask();
        if (!skillMatcher.hasBusinessMatch(task)) {
            return AgentResult.success("未匹配到可执行的技能，当前需求暂不支持自动开发。请先将该需求转换为 skill 后再提交。");
        }

        return agent.run(
                task
        );


    }



}

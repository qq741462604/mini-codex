package com.minicodex.interfaces.web;


import com.minicodex.application.agent.Agent;
import com.minicodex.domain.agent.AgentResult;
import com.minicodex.application.agent.AgentBatchService;
import com.minicodex.application.skill.SkillMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.minicodex.domain.skill.Skill;



@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
/**
 * AgentController：负责 Agent HTTP 请求与响应的协议适配。
 * 所属层：接口层。
 *
 * @author yy
 */
public class AgentController {



    private final Agent agent;

    private final SkillMatcher skillMatcher;

    private final AgentBatchService batchService;



    @PostMapping("/run")
    public AgentResult run(
            @RequestBody AgentRequest request
    ){

        if (batchService.isBatchRequest(request)) {
            return batchService.run(request);
        }

        String task = request == null ? null : request.getTask();
        if (!skillMatcher.hasBusinessMatch(task)) {
            return AgentResult.success("未匹配到可执行的技能，当前需求暂不支持自动开发。请先将该需求转换为 skill 后再提交。");
        }

        return agent.run(
                task
        );


    }



}

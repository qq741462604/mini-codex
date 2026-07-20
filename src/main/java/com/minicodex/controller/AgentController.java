package com.minicodex.controller;


import com.minicodex.agent.Agent;
import com.minicodex.agent.AgentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {



    private final Agent agent;



    @PostMapping("/run")
    public AgentResult run(
            @RequestBody AgentRequest request
    ){


        return agent.run(
                request.getTask()
        );


    }



}
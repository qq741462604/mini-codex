package com.minicodex.controller;


import com.minicodex.agent.Agent;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/codex")
public class ChatController {



    private final Agent agent;



    public ChatController(
            Agent agent
    ){

        this.agent = agent;

    }



    @PostMapping("/chat")
    public String chat(
            @RequestBody String message
    ){

        return agent.run(
                message
        );

    }


}
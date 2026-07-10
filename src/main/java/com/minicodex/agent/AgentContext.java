package com.minicodex.agent;


import lombok.Data;


import java.util.HashMap;
import java.util.Map;


@Data
public class AgentContext {


    private String userInput;


    private Map<String,Object> variables =
            new HashMap<>();


}
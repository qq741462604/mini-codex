package com.minicodex.tool;


import org.springframework.stereotype.Component;


import java.util.*;


@Component
public class ToolRegistry {



    private final Map<String,Tool> tools =
            new HashMap<>();



    public ToolRegistry(
            List<Tool> toolList
    ){


        for(Tool tool:toolList){

            register(tool);

        }


    }



    public void register(
            Tool tool
    ){

        tools.put(
                tool.name(),
                tool
        );

    }



    public Tool get(
            String name
    ){

        return tools.get(name);

    }



    public Collection<Tool> list(){

        return tools.values();

    }


}
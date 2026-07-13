package com.minicodex.agent;


import com.minicodex.ai.AiClient;
import com.minicodex.ai.AiMessage;
import com.minicodex.ai.AiResponse;
import com.minicodex.ai.PromptBuilder;
import com.minicodex.ai.ToolCall;

import com.minicodex.tool.Tool;
import com.minicodex.tool.ToolRegistry;
import com.minicodex.tool.ToolResult;


import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Component;



import java.util.ArrayList;
import java.util.List;



@Slf4j
@Component
public class Agent {



    private final AiClient aiClient;


    private final ToolRegistry toolRegistry;


    private final PromptBuilder promptBuilder;



    /**
     * 最大执行次数
     */
    private static final int MAX_STEP = 5;




    public Agent(
            AiClient aiClient,
            ToolRegistry toolRegistry,
            PromptBuilder promptBuilder
    ){

        this.aiClient = aiClient;

        this.toolRegistry = toolRegistry;

        this.promptBuilder = promptBuilder;

    }





    public String run(
            String input
    ){


        AgentContext context =
                new AgentContext();


        context.setUserInput(
                input
        );



        initContext(
                context
        );



        for(int i=0;i<MAX_STEP;i++){


            context.setStep(i);



            log.info(
                    "Agent执行步骤:{}",
                    i
            );



            AiResponse response =
                    aiClient.chat(
                            context.getMessages()
                    );



            /*
             * 没有工具调用
             * 直接返回
             */
            if(response.getToolCall()==null){


                log.info(
                        "Agent任务完成"
                );


                return response.getContent();

            }



            ToolCall call =
                    response.getToolCall();



            log.info(
                    "调用工具:{}",
                    call.getToolName()
            );



            executeTool(
                    context,
                    call
            );


        }



        return "任务执行次数超过限制";

    }







    private void initContext(
            AgentContext context
    ){


        AiMessage system =
                new AiMessage();


        system.setRole(
                "system"
        );


        system.setContent(
                promptBuilder
                        .buildSystemPrompt()
        );


        context.getMessages()
                .add(system);



        AiMessage user =
                new AiMessage();


        user.setRole(
                "user"
        );


        user.setContent(
                context.getUserInput()
        );


        context.getMessages()
                .add(user);


    }








    private void executeTool(
            AgentContext context,
            ToolCall call
    ){


        Tool tool =
                toolRegistry.get(
                        call.getToolName()
                );



        if(tool==null){


            AiMessage message =
                    new AiMessage();


            message.setRole(
                    "tool"
            );


            message.setContent(
                    "工具不存在:"
                            +call.getToolName()
            );


            context.getMessages()
                    .add(message);



            return;

        }




        ToolResult result =
                tool.execute(
                        call.getArguments()
                );



        AiMessage toolMessage =
                new AiMessage();


        toolMessage.setRole(
                "tool"
        );


        if(result.isSuccess()){


            toolMessage.setContent(
                    result.getOutput()
            );


        }
        else{


            toolMessage.setContent(
                    "执行失败:"
                            +result.getOutput()
            );


        }



        context.getMessages()
                .add(toolMessage);


    }


}